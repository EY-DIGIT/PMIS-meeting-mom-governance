package com.uidai.governance.mom.dto;

import com.uidai.governance.mom.domain.ActionItemComment;
import java.time.Instant;

/** A comment in an action item's threaded history (MEET-FR-04.4). */
public record CommentDto(
        Long id,
        Long parentId,
        String authorUserId,
        String content,
        Instant createdAt
) {
    public static CommentDto from(ActionItemComment c) {
        return new CommentDto(c.getId(), c.getParentId(), c.getAuthorUserId(), c.getContent(), c.getCreatedAt());
    }
}
