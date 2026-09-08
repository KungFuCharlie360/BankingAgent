package com.example.customerrouting.websocket;

import java.time.*;
import java.util.*;

public record DomainEvent(String eventType, UUID enquiryId, UUID agentId, Map<String, Object> payload,
        Instant timestamp) {
    public DomainEvent(String t, UUID e, UUID a, Map<String, Object> p) {
        this(t, e, a, p, Instant.now());
    }
}
