package com.uidai.governance.mom.domain;

import com.uidai.governance.common.audit.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * An action item assigned from a MoM (MEET-FR-04.2). Carries a threaded comment
 * history (MEET-FR-04.4) and timeline extension requests (MEET-FR-04.5), and can
 * be linked to a task, milestone and ticket (MEET-FR-05.2).
 */
@Entity
@Table(name = "action_item", indexes = {
        @Index(name = "idx_action_assignee", columnList = "assigned_to_user_id"),
        @Index(name = "idx_action_status", columnList = "status")
})
public class ActionItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mom_id", nullable = false)
    private MinutesOfMeeting mom;

    @Column(name = "description", nullable = false, length = 4000)
    private String description;

    /** Assignee (external User service id). */
    @Column(name = "assigned_to_user_id", nullable = false, length = 100)
    private String assignedToUserId;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ActionItemStatus status = ActionItemStatus.OPEN;

    // --- Links to project artefacts (MEET-FR-05.2) ---
    @Column(name = "linked_task_id")
    private Long linkedTaskId;

    @Column(name = "linked_milestone_id")
    private Long linkedMilestoneId;

    @Column(name = "linked_ticket_id", length = 100)
    private String linkedTicketId;

    /** Link to an external project activity (GET/PATCH /activities/{id}). */
    @Column(name = "linked_activity_id", length = 100)
    private String linkedActivityId;

    @OneToMany(mappedBy = "actionItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ActionItemComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "actionItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("requestedAt ASC")
    private List<TimelineExtensionRequest> extensionRequests = new ArrayList<>();

    protected ActionItem() {
    }

    public ActionItem(String description, String assignedToUserId, LocalDate dueDate) {
        this.description = description;
        this.assignedToUserId = assignedToUserId;
        this.dueDate = dueDate;
        this.status = ActionItemStatus.OPEN;
    }

    public void addComment(ActionItemComment comment) {
        comment.setActionItem(this);
        this.comments.add(comment);
    }

    public void addExtensionRequest(TimelineExtensionRequest request) {
        request.setActionItem(this);
        this.extensionRequests.add(request);
    }

    public Long getId() {
        return id;
    }

    public MinutesOfMeeting getMom() {
        return mom;
    }

    public void setMom(MinutesOfMeeting mom) {
        this.mom = mom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(String assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public ActionItemStatus getStatus() {
        return status;
    }

    public void setStatus(ActionItemStatus status) {
        this.status = status;
    }

    public Long getLinkedTaskId() {
        return linkedTaskId;
    }

    public void setLinkedTaskId(Long linkedTaskId) {
        this.linkedTaskId = linkedTaskId;
    }

    public Long getLinkedMilestoneId() {
        return linkedMilestoneId;
    }

    public void setLinkedMilestoneId(Long linkedMilestoneId) {
        this.linkedMilestoneId = linkedMilestoneId;
    }

    public String getLinkedTicketId() {
        return linkedTicketId;
    }

    public void setLinkedTicketId(String linkedTicketId) {
        this.linkedTicketId = linkedTicketId;
    }

    public String getLinkedActivityId() {
        return linkedActivityId;
    }

    public void setLinkedActivityId(String linkedActivityId) {
        this.linkedActivityId = linkedActivityId;
    }

    public List<ActionItemComment> getComments() {
        return comments;
    }

    public List<TimelineExtensionRequest> getExtensionRequests() {
        return extensionRequests;
    }
}
