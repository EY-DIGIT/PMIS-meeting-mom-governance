package com.uidai.governance.mom.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/**
 * A request to extend an action item's timeline (MEET-FR-04.5).
 *
 * <p>Requires a documented justification and is approved/rejected by a
 * designated authority; both the request and the decision are audit-logged
 * separately via the central audit trail.</p>
 */
@Entity
@Table(name = "timeline_extension_request")
public class TimelineExtensionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "action_item_id", nullable = false)
    private ActionItem actionItem;

    @Column(name = "previous_due_date")
    private LocalDate previousDueDate;

    @Column(name = "requested_due_date", nullable = false)
    private LocalDate requestedDueDate;

    /** Mandatory documented justification (MEET-FR-04.5). */
    @Column(name = "justification", nullable = false, length = 4000)
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ExtensionRequestStatus status = ExtensionRequestStatus.PENDING;

    @Column(name = "requested_by_user_id", nullable = false, length = 100)
    private String requestedByUserId;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    /** Designated approving authority (external User service id). */
    @Column(name = "decided_by_user_id", length = 100)
    private String decidedByUserId;

    @Column(name = "decision_comment", length = 2000)
    private String decisionComment;

    @Column(name = "decided_at")
    private Instant decidedAt;

    protected TimelineExtensionRequest() {
    }

    public TimelineExtensionRequest(LocalDate previousDueDate, LocalDate requestedDueDate,
                                    String justification, String requestedByUserId, Instant requestedAt) {
        this.previousDueDate = previousDueDate;
        this.requestedDueDate = requestedDueDate;
        this.justification = justification;
        this.requestedByUserId = requestedByUserId;
        this.requestedAt = requestedAt;
        this.status = ExtensionRequestStatus.PENDING;
    }

    public void decide(ExtensionRequestStatus decision, String decidedByUserId,
                       String decisionComment, Instant decidedAt) {
        this.status = decision;
        this.decidedByUserId = decidedByUserId;
        this.decisionComment = decisionComment;
        this.decidedAt = decidedAt;
    }

    public Long getId() {
        return id;
    }

    public ActionItem getActionItem() {
        return actionItem;
    }

    public void setActionItem(ActionItem actionItem) {
        this.actionItem = actionItem;
    }

    public LocalDate getPreviousDueDate() {
        return previousDueDate;
    }

    public LocalDate getRequestedDueDate() {
        return requestedDueDate;
    }

    public String getJustification() {
        return justification;
    }

    public ExtensionRequestStatus getStatus() {
        return status;
    }

    public String getRequestedByUserId() {
        return requestedByUserId;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public String getDecidedByUserId() {
        return decidedByUserId;
    }

    public String getDecisionComment() {
        return decisionComment;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
