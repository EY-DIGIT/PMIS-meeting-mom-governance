package com.uidai.governance.common.audit;

/**
 * Types of auditable actions recorded in the immutable audit trail.
 */
public enum AuditAction {
    MEETING_CREATED,
    MEETING_UPDATED,
    MEETING_STATUS_CHANGED,
    MOM_CREATED,
    MOM_UPDATED,
    MOM_FINALIZED,
    ACTION_ITEM_CREATED,
    ACTION_ITEM_UPDATED,
    ACTION_ITEM_COMMENT_ADDED,
    EXTENSION_REQUESTED,
    EXTENSION_APPROVED,
    EXTENSION_REJECTED
}
