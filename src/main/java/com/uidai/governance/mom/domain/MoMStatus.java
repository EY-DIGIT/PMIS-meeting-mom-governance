package com.uidai.governance.mom.domain;

import java.util.Set;

/**
 * Lifecycle of a Minutes-of-Meeting record. A FINALIZED MoM forms part of the
 * official UIDAI project record and is immutable (MEET-FR-05.3).
 */
public enum MoMStatus {
    DRAFT,
    IN_REVIEW,
    FINALIZED;

    private static final java.util.Map<MoMStatus, Set<MoMStatus>> TRANSITIONS = java.util.Map.of(
            DRAFT, Set.of(IN_REVIEW, FINALIZED),
            IN_REVIEW, Set.of(DRAFT, FINALIZED),
            FINALIZED, Set.of()
    );

    public boolean canTransitionTo(MoMStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isEditable() {
        return this != FINALIZED;
    }
}
