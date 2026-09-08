package com.example.customerrouting.simulation;

import com.example.customerrouting.assignment.*;
import com.example.customerrouting.conversation.*;
import com.example.customerrouting.enquiry.*;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {
    private final CustomerSimulationService customers;
    private final SimulationScheduler scheduler;
    private final SimulatedEnquiryStateRepository states;
    private final EnquiryRepository enquiries;
    private final ConversationMessageRepository messages;
    private final AssignmentHistoryRepository history;

    public SimulationController(CustomerSimulationService c, SimulationScheduler s, SimulatedEnquiryStateRepository st,
            EnquiryRepository e, ConversationMessageRepository m, AssignmentHistoryRepository h) {
        customers = c;
        scheduler = s;
        states = st;
        enquiries = e;
        messages = m;
        history = h;
    }

    record Status(boolean running, long activeTimers, long waiting) {
    }

    record SimulatedEnquiryView(UUID enquiryId, UUID agentId, Instant simulatedCompletionAt,
            int handlingDurationSeconds, SimulationStateStatus status) {
    }

    record Dashboard(boolean running, long activeTimers, long waiting, List<SimulatedEnquiryView> simulatedEnquiries) {
    }

    @PostMapping("/enquiries")
    public Map<String, UUID> one() {
        return Map.of("enquiryId", customers.create().getId());
    }

    @PostMapping("/enquiries/batch")
    public List<UUID> batch(@RequestParam(defaultValue = "20") int count) {
        if (count < 1 || count > 500)
            throw new IllegalArgumentException("count must be between 1 and 500");
        return customers.batch(count).stream().map(Enquiry::getId).toList();
    }

    @PostMapping("/start")
    public void start() {
        scheduler.start();
    }

    @PostMapping("/stop")
    public void stop() {
        scheduler.stop();
    }

    @GetMapping("/status")
    public Status status() {
        return new Status(scheduler.running(), states.findByStatus(SimulationStateStatus.HANDLING).size(),
                states.findByStatus(SimulationStateStatus.WAITING_FOR_AGENT).size());
    }

    @GetMapping("/dashboard")
    public Dashboard dashboard() {
        List<SimulatedEnquiryView> views = states.findAll().stream().map(x -> new SimulatedEnquiryView(x.getEnquiryId(),
                x.getAgentId(), x.getSimulatedCompletionAt(), x.getHandlingDurationSeconds(), x.getStatus())).toList();
        return new Dashboard(scheduler.running(),
                views.stream().filter(x -> x.status() == SimulationStateStatus.HANDLING).count(),
                views.stream().filter(x -> x.status() == SimulationStateStatus.WAITING_FOR_AGENT).count(), views);
    }

    @PostMapping("/reset")
    @Transactional
    public void reset() {
        scheduler.stop();
        List<UUID> ids = enquiries.findBySource(EnquirySource.SIMULATION).stream().map(Enquiry::getId).toList();
        messages.deleteByEnquiryIdIn(ids);
        history.deleteByEnquiryIdIn(ids);
        states.deleteAll();
        enquiries.deleteAllById(ids);
    }
}
