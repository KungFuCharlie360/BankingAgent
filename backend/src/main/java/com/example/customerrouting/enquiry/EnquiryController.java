package com.example.customerrouting.enquiry;

import com.example.customerrouting.agent.*;
import com.example.customerrouting.conversation.*;
import com.example.customerrouting.routing.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/enquiries")
public class EnquiryController {
    private final EnquiryService s;
    private final ConversationService conv;

    public EnquiryController(EnquiryService s, ConversationService c) {
        this.s = s;
        conv = c;
    }

    record Create(@NotBlank String customerId, @NotNull EnquiryCategory category, @NotNull Language preferredLanguage,
            @NotBlank String message) {
    }

    record Message(@NotNull SenderType senderType, @NotBlank String senderId, @NotBlank String content) {
    }

    record AgentBrief(UUID id, String name) {
    }

    record View(UUID enquiryId, String customerId, EnquiryCategory category, Language preferredLanguage,
            EnquiryStatus status, AgentBrief assignedAgent, Instant createdAt, Instant assignedAt,
            Instant lastCustomerActivityAt, Instant closedAt) {
    }

    record MessageView(UUID messageId, UUID enquiryId, SenderType senderType, String senderId, String content,
            Instant createdAt) {
    }

    private View v(Enquiry e) {
        return new View(e.getId(), e.getCustomerId(), e.getCategory(), e.getPreferredLanguage(), e.getStatus(),
                e.getAssignedAgent() == null ? null
                        : new AgentBrief(e.getAssignedAgent().getId(), e.getAssignedAgent().getName()),
                e.getCreatedAt(), e.getAssignedAt(), e.getLastCustomerActivityAt(), e.getClosedAt());
    }

    private MessageView m(ConversationMessage x) {
        return new MessageView(x.getId(), x.getEnquiry().getId(), x.getSenderType(), x.getSenderId(), x.getContent(),
                x.getCreatedAt());
    }

    @PostMapping
    public View create(@Valid @RequestBody Create r) {
        return v(s.create(r.customerId(), r.category(), r.preferredLanguage(), r.message()));
    }

    @GetMapping
    public List<View> all(@RequestParam(required = false) EnquiryStatus status,
            @RequestParam(required = false) UUID agentId, @RequestParam(required = false) String customerId,
            @RequestParam(required = false) EnquiryCategory category,
            @RequestParam(required = false) Language preferredLanguage) {
        return s.all(status, agentId, customerId, category, preferredLanguage).stream().map(this::v).toList();
    }

    @GetMapping("/pending")
    public List<View> pending() {
        return s.all(EnquiryStatus.PENDING, null, null, null, null).stream().map(this::v).toList();
    }

    @GetMapping("/{id}")
    public View one(@PathVariable UUID id) {
        return v(s.one(id));
    }

    @PostMapping("/{id}/close")
    public View close(@PathVariable UUID id) {
        return v(s.close(id, "MANUAL_CLOSE"));
    }

    @PostMapping("/{id}/messages")
    public MessageView post(@PathVariable UUID id, @Valid @RequestBody Message r) {
        return m(conv.post(id, r.senderType(), r.senderId(), r.content()));
    }

    @GetMapping("/{id}/messages")
    public List<MessageView> messages(@PathVariable UUID id) {
        return conv.history(id).stream().map(this::m).toList();
    }
}
