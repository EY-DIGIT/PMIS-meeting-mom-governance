package com.uidai.governance.meeting.domain;

import java.util.Set;

/**
 * Lifecycle of a meeting record. Each meeting type can layer its own approval
 * workflow on top of these states (MEET-FR-02.2).
 */
public enum MeetingStatus {
    DRAFT,
    SCHEDULED,
    COMPLETED,
    CANCELLED;

    private static final java.util.Map<MeetingStatus, Set<MeetingStatus>> TRANSITIONS = java.util.Map.of(
            DRAFT, Set.of(SCHEDULED, CANCELLED),
            SCHEDULED, Set.of(COMPLETED, CANCELLED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of()
    );

    public boolean canTransitionTo(MeetingStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
