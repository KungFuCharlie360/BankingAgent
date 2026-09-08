package com.example.customerrouting.websocket;

import org.springframework.messaging.simp.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.event.*;

@Component
public class WebSocketEventListener {
    private final SimpMessagingTemplate t;

    public WebSocketEventListener(SimpMessagingTemplate t) {
        this.t = t;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sent(DomainEvent e) {
        t.convertAndSend("/topic/routing", e);
        if (e.enquiryId() != null)
            t.convertAndSend("/topic/enquiries/" + e.enquiryId(), e);
        if (e.agentId() != null)
            t.convertAndSend("/topic/agents/" + e.agentId() + "/assignments", e);
    }
}
