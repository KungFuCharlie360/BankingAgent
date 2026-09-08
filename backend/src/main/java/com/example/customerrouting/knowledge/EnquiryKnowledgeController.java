package com.example.customerrouting.knowledge;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/enquiries")
public class EnquiryKnowledgeController {
    private final KnowledgeBaseService s;

    public EnquiryKnowledgeController(KnowledgeBaseService s) {
        this.s = s;
    }

    @GetMapping("/{id}/knowledge")
    public List<BankingFaqEntry> relevant(@PathVariable UUID id) {
        return s.relevant(id);
    }
}
