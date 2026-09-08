package com.example.customerrouting;

import com.example.customerrouting.agent.*;
import com.example.customerrouting.enquiry.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApplicationIntegrationTest {
  @Autowired EnquiryService enquiries;
  @Autowired AgentRepository agents;
  @Test void createsAndRoutesCompatibleEnquiry() {
    Enquiry enquiry=enquiries.create("TEST-CUSTOMER",EnquiryCategory.CARD_PAYMENT_DISPUTE,Language.ENGLISH,"Unrecognised payment");
    assertEquals(EnquiryStatus.ASSIGNED,enquiry.getStatus());
    assertNotNull(enquiry.getAssignedAgent());
  }
}
