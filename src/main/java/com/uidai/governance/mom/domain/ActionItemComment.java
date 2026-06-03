package com.uidai.governance.mom.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A comment in the threaded comment history of an action item (MEET-FR-04.4).
 *
 * <p>Threading is modelled by a self-reference to {@code parent}; top-level
 * comments have a null parent and replies point at the comment they answer.</p>
 */
@Entity
@Table(name = "action_item_comment", indexes = {
        @Index(name = "idx_comment_action", columnList = "action_item_id"),
        @Index(name = "idx_comment_parent", columnList = "parent_comment_id")
})
public class ActionItemComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "action_item_id", nullable = false)
    private ActionItem actionItem;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private ActionItemComment parent;

    @Column(name = "author_user_id", nullable = false, length = 100)
    private String authorUserId;

    @Column(name = "content", nullable = false, length = 4000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ActionItemComment() {
    }

    public ActionItemComment(String authorUserId, String content, ActionItemComment parent, Instant createdAt) {
        this.authorUserId = authorUserId;
        this.content = content;
        this.parent = parent;
        this.createdAt = createdAt;
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

    public ActionItemComment getParent() {
        return parent;
    }

    public Long getParentId() {
        return parent != null ? parent.getId() : null;
    }

    public String getAuthorUserId() {
        return authorUserId;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
