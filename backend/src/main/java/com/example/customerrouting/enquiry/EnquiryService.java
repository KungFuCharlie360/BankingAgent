package com.example.customerrouting.enquiry;

import com.example.customerrouting.agent.*;
import com.example.customerrouting.assignment.*;
import com.example.customerrouting.conversation.*;
import com.example.customerrouting.routing.*;
import com.example.customerrouting.websocket.*;
import jakarta.transaction.*;
import org.springframework.stereotype.*;
import java.util.*;

@Service
public class EnquiryService {
    private final EnquiryRepository repo;
    private final ConversationMessageRepository messages;
    private final RoutingService routing;
    private final AgentRepository agents;
    private final AssignmentHistoryRepository history;
    private final PendingEnquiryService pending;
    private final EventPublisher events;

    public EnquiryService(EnquiryRepository r, ConversationMessageRepository m, RoutingService ro, AgentRepository a,
            AssignmentHistoryRepository h, PendingEnquiryService p, EventPublisher ev) {
        repo = r;
        messages = m;
        routing = ro;
        agents = a;
        history = h;
        pending = p;
        events = ev;
    }

    @Transactional
    public Enquiry create(String customer, EnquiryCategory c, Language l, String text) {
        return createInternal(customer, c, l, text, EnquirySource.MANUAL);
    }

    @Transactional
    public Enquiry createSimulation(String customer, EnquiryCategory c, Language l, String text) {
        return createInternal(customer, c, l, text, EnquirySource.SIMULATION);
    }

    private Enquiry createInternal(String customer, EnquiryCategory c, Language l, String text, EnquirySource source) {
        Enquiry e = new Enquiry(customer, c, l);
        e.setSource(source);
        repo.save(e);
        messages.save(new ConversationMessage(e, SenderType.CUSTOMER, customer, text));
        events.publish(new DomainEvent("ENQUIRY_CREATED", e.getId(), null, Map.of("category", c, "source", source)));
        return routing.route(e.getId(), "INITIAL_ROUTING");
    }

    public Enquiry one(UUID id) {
        return repo.findByIdWithAssignedAgent(id).orElseThrow(() -> new NoSuchElementException("Enquiry not found"));
    }

    public List<Enquiry> all(EnquiryStatus s, UUID agent, String customer, EnquiryCategory c, Language l) {
        return repo.findAllWithAssignedAgent().stream().filter(e -> s == null || e.getStatus() == s)
                .filter(e -> agent == null
                        || (e.getAssignedAgent() != null && agent.equals(e.getAssignedAgent().getId())))
                .filter(e -> customer == null || customer.equals(e.getCustomerId()))
                .filter(e -> c == null || c == e.getCategory()).filter(e -> l == null || l == e.getPreferredLanguage())
                .toList();
    }

    @Transactional
    public Enquiry close(UUID id, String reason) {
        Enquiry e = one(id);
        if (!e.close())
            return e;
        Agent a = e.getAssignedAgent();
        if (a != null) {
            Agent locked = agents.lockById(a.getId()).orElseThrow();
            locked.release();
            history.save(new AssignmentHistory(e.getId(), locked.getId(), AssignmentType.UNASSIGNED, 0, 0, reason));
        }
        events.publish(
                new DomainEvent("ENQUIRY_CLOSED", e.getId(), a == null ? null : a.getId(), Map.of("reason", reason)));
        pending.retry();
        return e;
    }

    @Transactional
    public Enquiry unassignForOffline(UUID id, String reason) {
        Enquiry e = one(id);
        Agent a = e.getAssignedAgent();
        if (a == null)
            return e;
        agents.lockById(a.getId()).orElseThrow().release();
        history.save(new AssignmentHistory(e.getId(), a.getId(), AssignmentType.UNASSIGNED, 0, 0, reason));
        e.pending();
        return routing.route(id, reason);
    }
}
