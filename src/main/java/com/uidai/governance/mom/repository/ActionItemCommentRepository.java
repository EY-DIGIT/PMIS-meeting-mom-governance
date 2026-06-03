package com.uidai.governance.mom.repository;

import com.uidai.governance.mom.domain.ActionItemComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionItemCommentRepository extends JpaRepository<ActionItemComment, Long> {

    List<ActionItemComment> findByActionItemIdOrderByCreatedAtAsc(Long actionItemId);
}
