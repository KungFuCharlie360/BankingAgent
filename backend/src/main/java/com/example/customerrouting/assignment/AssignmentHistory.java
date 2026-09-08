package com.example.customerrouting.assignment;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
public class AssignmentHistory {
    @Id
    private UUID id = UUID.randomUUID();
    private UUID enquiryId, agentId;
    @Enumerated(EnumType.STRING)
    private AssignmentType type;
    private double matchScore, utilizationAtAssignment;
    private String reason;
    private Instant createdAt = Instant.now();

    protected AssignmentHistory() {
    }

    public AssignmentHistory(UUID e, UUID a, AssignmentType t, double s, double u, String r) {
        enquiryId = e;
        agentId = a;
        type = t;
        matchScore = s;
        utilizationAtAssignment = u;
        reason = r;
    }
}
