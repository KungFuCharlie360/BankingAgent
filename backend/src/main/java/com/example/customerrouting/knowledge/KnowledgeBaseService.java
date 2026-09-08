package com.example.customerrouting.knowledge;

import com.example.customerrouting.conversation.*;
import com.example.customerrouting.enquiry.*;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.*;
import java.util.*;

@Service
public class KnowledgeBaseService {
    private final ConversationMessageRepository messages;
    private final EnquiryService enquiries;
    private final List<BankingFaqEntry> entries = List.of(
            new BankingFaqEntry("card-payment", EnquiryCategory.CARD_PAYMENT_DISPUTE,
                    "How do I dispute a card payment?",
                    "Review the transaction and submit a dispute after confirming it is unrecognised."),
            new BankingFaqEntry("transfer", EnquiryCategory.BANK_TRANSFER, "Why is my transfer pending?",
                    "Transfers can take time while beneficiary and compliance checks complete."),
            new BankingFaqEntry("identity", EnquiryCategory.IDENTITY_VERIFICATION, "Why verify identity?",
                    "Identity verification helps protect your account and meet banking regulations."));

    public KnowledgeBaseService(ConversationMessageRepository m, EnquiryService e) {
        messages = m;
        enquiries = e;
    }

    @Cacheable("faqEntries")
    public List<BankingFaqEntry> all() {
        return entries;
    }

    @Cacheable("faqSearch")
    public List<BankingFaqEntry> search(String q) {
        String x = q.toLowerCase();
        return entries.stream()
                .filter(e -> (e.question() + " " + e.answer() + " " + e.category()).toLowerCase().contains(x)).toList();
    }

    @Cacheable("faqCategory")
    public List<BankingFaqEntry> category(EnquiryCategory c) {
        return entries.stream().filter(e -> e.category() == c).toList();
    }

    public List<BankingFaqEntry> relevant(UUID enquiryId) {
        Enquiry e = enquiries.one(enquiryId);
        String words = messages.findByEnquiryIdOrderByCreatedAtAsc(enquiryId).stream()
                .filter(m -> m.getSenderType() == SenderType.CUSTOMER).map(ConversationMessage::getContent)
                .reduce("", String::concat);
        LinkedHashSet<BankingFaqEntry> x = new LinkedHashSet<>(category(e.getCategory()));
        for (String word : words.split("\\W+")) {
            if (word.length() > 3)
                x.addAll(search(word));
        }
        return x.stream().limit(5).toList();
    }
}
