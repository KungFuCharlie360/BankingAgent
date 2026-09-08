package com.example.customerrouting.simulation;

import com.example.customerrouting.enquiry.*;
import com.example.customerrouting.simulation.banking77.*;
import com.example.customerrouting.simulation.random.*;
import com.example.customerrouting.websocket.*;
import jakarta.transaction.*;
import org.springframework.stereotype.*;
import java.util.*;

@Service
public class CustomerSimulationService {
    private final Banking77DatasetLoader data;
    private final Banking77IntentMapping map;
    private final LanguageSelector languages;
    private final EnquiryService enquiries;
    private final SimulatedEnquiryStateRepository states;
    private final SimulationCompletionService completion;
    private final EventPublisher events;

    public CustomerSimulationService(Banking77DatasetLoader d, Banking77IntentMapping m, LanguageSelector l,
            EnquiryService e, SimulatedEnquiryStateRepository s, SimulationCompletionService c, EventPublisher p) {
        data = d;
        map = m;
        languages = l;
        enquiries = e;
        states = s;
        completion = c;
        events = p;
    }

    @Transactional
    public Enquiry create() {
        Banking77Sample x = data.random();
        Enquiry e = enquiries.createSimulation("SIM-" + UUID.randomUUID().toString().substring(0, 8),
                map.categoryFor(x.intent()), languages.next(), x.text());
        states.save(new SimulatedEnquiryState(e.getId()));
        completion.reconcile(e.getId());
        events.publish(new DomainEvent("SIMULATION_ENQUIRY_CREATED", e.getId(),
                e.getAssignedAgent() == null ? null : e.getAssignedAgent().getId(),
                Map.of("intent", x.intent(), "category", e.getCategory())));
        return e;
    }

    public List<Enquiry> batch(int count) {
        List<Enquiry> x = new ArrayList<>();
        for (int i = 0; i < count; i++)
            x.add(create());
        return x;
    }
}
