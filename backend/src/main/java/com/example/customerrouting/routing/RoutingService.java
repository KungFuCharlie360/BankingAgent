package com.example.customerrouting.routing;

import com.example.customerrouting.agent.*;
import com.example.customerrouting.assignment.*;
import com.example.customerrouting.enquiry.*;
import com.example.customerrouting.websocket.*;
import jakarta.transaction.*;
import org.slf4j.*;
import org.springframework.stereotype.*;
import java.util.*;

@Service
public class RoutingService {
    private static final Logger log = LoggerFactory.getLogger(RoutingService.class);
    private final EnquiryRepository enquiries;
    private final AgentRepository agents;
    private final AssignmentHistoryRepository history;
    private final CategorySkillService categories;
    private final SkillMatcher matcher;
    private final EventPublisher events;

    public RoutingService(EnquiryRepository e, AgentRepository a, AssignmentHistoryRepository h, CategorySkillService c,
            SkillMatcher m, EventPublisher p) {
        enquiries = e;
        agents = a;
        history = h;
        categories = c;
        matcher = m;
        events = p;
    }

    @Transactional
    public Enquiry route(UUID id, String reason) {
        Enquiry e = enquiries.findById(id).orElseThrow(() -> new NoSuchElementException("Enquiry not found"));
        if (e.getStatus() != EnquiryStatus.PENDING)
            return e;
        Set<String> required = categories.required(e.getCategory());
        List<Candidate> cs = agents.findByStatus(AgentStatus.ONLINE).stream()
                .filter(a -> a.getLanguages().contains(e.getPreferredLanguage()))
                .filter(a -> a.getActiveEnquiryCount() < a.getMaxCapacity())
                .map(a -> new Candidate(a, matcher.containment(required, a.getSkills())))
                .filter(c -> matcher.qualifies(c.score))
                .sorted(Comparator
                        .comparingDouble(
                                (Candidate c) -> (double) c.agent.getActiveEnquiryCount() / c.agent.getMaxCapacity())
                        .thenComparing(c -> c.agent.getLastAssignedAt(),
                                Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(c -> c.agent.getId()))
                .toList();
        for (Candidate c : cs) {
            Agent locked = agents.lockById(c.agent.getId()).orElse(null);
            if (locked == null || locked.getStatus() != AgentStatus.ONLINE
                    || locked.getActiveEnquiryCount() >= locked.getMaxCapacity()
                    || !locked.getLanguages().contains(e.getPreferredLanguage()))
                continue;
            double score = matcher.containment(required, locked.getSkills());
            if (!matcher.qualifies(score))
                continue;
            locked.claim();
            e.assign(locked);
            double u = (double) locked.getActiveEnquiryCount() / locked.getMaxCapacity();
            history.save(new AssignmentHistory(e.getId(), locked.getId(), AssignmentType.INITIAL_ASSIGNMENT, score, u,
                    reason));
            events.publish(new DomainEvent("ENQUIRY_ASSIGNED", e.getId(), locked.getId(),
                    Map.of("agentName", locked.getName(), "category", e.getCategory(), "preferredLanguage",
                            e.getPreferredLanguage(), "skillMatchScore", score, "agentUtilization", u)));
            log.info("Routing enquiry={} selected={} score={} utilization={}", id, locked.getName(), score, u);
            return e;
        }
        e.pending();
        events.publish(new DomainEvent("ENQUIRY_PENDING", e.getId(), null,
                Map.of("category", e.getCategory(), "reason", reason)));
        log.info("Routing enquiry={} no eligible agent", id);
        return e;
    }

    private record Candidate(Agent agent, double score) {
    }
}
