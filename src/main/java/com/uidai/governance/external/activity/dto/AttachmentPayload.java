package com.uidai.governance.external.activity.dto;

/**
 * A file attachment sent to the PMIS activity-create API for a meeting milestone.
 *
 * <p>Field names are exact: {@code filename}, {@code contentType}, {@code content}.
 * {@code content} is standard base64 of the raw file bytes with no
 * {@code data:...;base64,} prefix (the frontend strips it). PMIS performs the
 * real validation (max 25 MB/file, allowed types, magic-byte check); this module
 * forwards the attachment as-is.</p>
 */
public record AttachmentPayload(
        String filename,
        String contentType,
        String content
) {
}
