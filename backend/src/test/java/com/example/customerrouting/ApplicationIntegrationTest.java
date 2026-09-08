package com.example.customerrouting;

import com.example.customerrouting.agent.*;
import com.example.customerrouting.enquiry.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationIntegrationTest {
  @Autowired EnquiryService enquiries;
  @Autowired AgentRepository agents;
  @Autowired MockMvc mvc;
  @Test void createsAndRoutesCompatibleEnquiry() {
    Enquiry enquiry=enquiries.create("TEST-CUSTOMER",EnquiryCategory.CARD_PAYMENT_DISPUTE,Language.ENGLISH,"Unrecognised payment");
    assertEquals(EnquiryStatus.ASSIGNED,enquiry.getStatus());
    assertNotNull(enquiry.getAssignedAgent());
  }

  @Test void listsAgentsWithLanguagesAndSkills() throws Exception {
    mvc.perform(get("/api/agents"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].languages").isArray())
        .andExpect(jsonPath("$[0].skills").isArray());
  }
}
