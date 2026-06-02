package com.uidai.governance.mom.repository;

import com.uidai.governance.mom.domain.ActionItem;
import com.uidai.governance.mom.domain.ActionItemStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {

    List<ActionItem> findByMomId(Long momId);

    Page<ActionItem> findByAssignedToUserId(String assignedToUserId, Pageable pageable);

    Page<ActionItem> findByStatus(ActionItemStatus status, Pageable pageable);
}
