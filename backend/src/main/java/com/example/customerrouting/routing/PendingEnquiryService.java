package com.example.customerrouting.routing;

import com.example.customerrouting.enquiry.*;
import org.springframework.stereotype.*;
import java.util.*;

@Service
public class PendingEnquiryService {
    private final EnquiryRepository repo;
    private final RoutingService routing;

    public PendingEnquiryService(EnquiryRepository r, RoutingService s) {
        repo = r;
        routing = s;
    }

    public void retry() {
        repo.findByStatusOrderByCreatedAtAsc(EnquiryStatus.PENDING)
                .forEach(e -> routing.route(e.getId(), "PENDING_RETRY"));
    }
}
