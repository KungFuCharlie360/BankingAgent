package com.example.customerrouting.conversation;

import com.example.customerrouting.enquiry.*;
import com.example.customerrouting.websocket.*;
import jakarta.transaction.*;
import org.springframework.stereotype.*;
import java.util.*;

@Service
public class ConversationService {
    private final ConversationMessageRepository repo;
    private final EnquiryService enquiries;
    private final EventPublisher events;

    public ConversationService(ConversationMessageRepository r, EnquiryService e, EventPublisher p) {
        repo = r;
        enquiries = e;
        events = p;
    }

    @Transactional
    public ConversationMessage post(UUID id, SenderType type, String sender, String content) {
        Enquiry e = enquiries.one(id);
        if (e.getStatus() == EnquiryStatus.CLOSED)
            throw new IllegalArgumentException("Conversation is closed");
        if (type == SenderType.CUSTOMER && !e.getCustomerId().equals(sender))
            throw new IllegalArgumentException("Customer senderId does not match enquiry");
        if (type == SenderType.AGENT
                && (e.getAssignedAgent() == null || !e.getAssignedAgent().getId().toString().equals(sender)))
            throw new IllegalArgumentException("Agent is not assigned to enquiry");
        if (type == SenderType.CUSTOMER)
            e.customerActivity();
        ConversationMessage m = repo.save(new ConversationMessage(e, type, sender, content));
        events.publish(new DomainEvent("MESSAGE_CREATED", id,
                e.getAssignedAgent() == null ? null : e.getAssignedAgent().getId(), Map.of("messageId", m.getId(),
                        "senderType", type, "senderId", sender, "content", content, "createdAt", m.getCreatedAt())));
        return m;
    }

    public List<ConversationMessage> history(UUID id) {
        enquiries.one(id);
        return repo.findByEnquiryIdOrderByCreatedAtAsc(id);
    }
}
