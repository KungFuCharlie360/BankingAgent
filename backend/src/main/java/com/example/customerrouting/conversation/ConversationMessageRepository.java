package com.example.customerrouting.conversation;

import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, UUID> {
    List<ConversationMessage> findByEnquiryIdOrderByCreatedAtAsc(UUID id);

    void deleteByEnquiryIdIn(Collection<UUID> ids);
}
