package com.example.customerrouting.enquiry;

import org.springframework.data.jpa.repository.*;
import java.time.*;
import java.util.*;

public interface EnquiryRepository extends JpaRepository<Enquiry, UUID> {
  @EntityGraph(attributePaths = "assignedAgent")
  @Query("select e from Enquiry e")
  List<Enquiry> findAllWithAssignedAgent();

  @EntityGraph(attributePaths = "assignedAgent")
  @Query("select e from Enquiry e where e.id = :id")
  Optional<Enquiry> findByIdWithAssignedAgent(UUID id);

  List<Enquiry> findByStatusOrderByCreatedAtAsc(EnquiryStatus s);
  List<Enquiry> findBySource(EnquirySource source);
  List<Enquiry> findByStatusInAndLastCustomerActivityAtBefore(Collection<EnquiryStatus>s,Instant t);
  List<Enquiry> findByAssignedAgentIdAndStatusIn(UUID id,Collection<EnquiryStatus>s);
}
