package com.example.customerrouting.knowledge;

import com.example.customerrouting.enquiry.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/knowledge-base")
public class KnowledgeBaseController {
    private final KnowledgeBaseService s;

    public KnowledgeBaseController(KnowledgeBaseService s) {
        this.s = s;
    }

    @GetMapping
    public List<BankingFaqEntry> all() {
        return s.all();
    }

    @GetMapping("/search")
    public List<BankingFaqEntry> search(@RequestParam String q) {
        return s.search(q);
    }

    @GetMapping("/category/{category}")
    public List<BankingFaqEntry> category(@PathVariable EnquiryCategory category) {
        return s.category(category);
    }
}
