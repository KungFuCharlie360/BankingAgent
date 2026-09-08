package com.example.customerrouting.simulation;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
public class SimulatedEnquiryState {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(unique = true, nullable = false)
    private UUID enquiryId;
    private UUID agentId;
    private Instant assignmentToken, handlingStartedAt, simulatedCompletionAt;
    private int handlingDurationSeconds;
    @Enumerated(EnumType.STRING)
    private SimulationStateStatus status = SimulationStateStatus.WAITING_FOR_AGENT;

    protected SimulatedEnquiryState() {
    }

    public SimulatedEnquiryState(UUID e) {
        enquiryId = e;
    }

    public UUID getEnquiryId() {
        return enquiryId;
    }

    public UUID getAgentId() {
        return agentId;
    }

    public Instant getAssignmentToken() {
        return assignmentToken;
    }

    public Instant getSimulatedCompletionAt() {
        return simulatedCompletionAt;
    }

    public int getHandlingDurationSeconds() {
        return handlingDurationSeconds;
    }

    public SimulationStateStatus getStatus() {
        return status;
    }

    public void start(UUID a, Instant token, int seconds) {
        agentId = a;
        assignmentToken = token;
        handlingStartedAt = Instant.now();
        handlingDurationSeconds = seconds;
        simulatedCompletionAt = handlingStartedAt.plusSeconds(seconds);
        status = SimulationStateStatus.HANDLING;
    }

    public void completed() {
        status = SimulationStateStatus.COMPLETED;
    }

    public void cancel() {
        status = SimulationStateStatus.CANCELLED;
    }
}
