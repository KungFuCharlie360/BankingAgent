package com.example.customerrouting.skill;

import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface SkillRepository extends JpaRepository<Skill, UUID> {
    Optional<Skill> findByCode(String code);

    List<Skill> findByCodeIn(Collection<String> codes);
}
