package com.example.customerrouting.conversation;

import com.example.customerrouting.enquiry.Enquiry;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
public class ConversationMessage {
    @Id
    private UUID id = UUID.randomUUID();
    @ManyToOne(fetch = FetchType.LAZY)
    private Enquiry enquiry;
    @Enumerated(EnumType.STRING)
    private SenderType senderType;
    private String senderId;
    @Column(length = 4000)
    private String content;
    private Instant createdAt = Instant.now();

    protected ConversationMessage() {
    }

    public ConversationMessage(Enquiry e, SenderType t, String s, String c) {
        enquiry = e;
        senderType = t;
        senderId = s;
        content = c;
    }

    public UUID getId() {
        return id;
    }

    public Enquiry getEnquiry() {
        return enquiry;
    }

    public SenderType getSenderType() {
        return senderType;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
