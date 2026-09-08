package com.example.customerrouting.agent;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface AgentRepository extends JpaRepository<Agent, UUID> {
  @EntityGraph(attributePaths = {"languages", "skills"})
  @Query("select distinct a from Agent a")
  List<Agent> findAllWithDetails();

  @EntityGraph(attributePaths = {"languages", "skills"})
  @Query("select distinct a from Agent a where a.id = :id")
  Optional<Agent> findByIdWithDetails(UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from Agent a where a.id=:id")
  Optional<Agent> lockById(UUID id);

  List<Agent> findByStatus(AgentStatus status);
}
