package com.uidai.governance.mom.repository;

import com.uidai.governance.mom.domain.MoMTemplate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoMTemplateRepository extends JpaRepository<MoMTemplate, Long> {

    List<MoMTemplate> findByActiveTrue();

    boolean existsByName(String name);
}
