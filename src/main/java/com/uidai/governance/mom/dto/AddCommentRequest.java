package com.uidai.governance.mom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to add a comment to an action item's threaded history (MEET-FR-04.4).
 * A non-null {@code parentCommentId} makes this comment a reply.
 */
public record AddCommentRequest(
        @NotBlank @Size(max = 100) String authorUserId,
        @NotBlank @Size(max = 4000) String content,
        Long parentCommentId
) {
}
