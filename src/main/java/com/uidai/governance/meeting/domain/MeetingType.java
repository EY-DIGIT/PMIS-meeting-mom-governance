package com.uidai.governance.meeting.domain;

import com.uidai.governance.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Configurable meeting category (MEET-FR-02, MEET-FR-02.4).
 *
 * <p>Seeded with Steering, Governance, Migration and Ad-hoc, but new categories
 * can be added at runtime via the administration API.</p>
 */
@Entity
@Table(name = "meeting_type")
public class MeetingType extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(name = "description", length = 500)
    private String description;

    /** Whether this type requires a formal approval step on its MoM workflow. */
    @Column(name = "requires_approval", nullable = false)
    private boolean requiresApproval = true;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected MeetingType() {
    }

    public MeetingType(String code, String displayName, String description, boolean requiresApproval) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
        this.requiresApproval = requiresApproval;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
