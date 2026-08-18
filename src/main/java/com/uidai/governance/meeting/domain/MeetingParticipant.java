package com.uidai.governance.meeting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * A participant of a meeting. Internal attendees ({@code external=false}) have a
 * {@code userId} validated against the external Python User service; external
 * attendees ({@code external=true}) are guests not held in the User service and
 * are stored as-is without validation.
 */
@Entity
@Table(name = "meeting_participant",
        uniqueConstraints = @UniqueConstraint(name = "uk_participant_meeting_user",
                columnNames = {"meeting_id", "user_id"}))
public class MeetingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    /**
     * Address the meeting invite is sent to. Mandatory on the API for internal
     * attendees; for external attendees it mirrors {@link #userId}, which already
     * holds their email address.
     */
    @Column(name = "email", length = 150)
    private String email;

    /** Role name of the attendee (optional, not validated against the User service). */
    @Column(name = "role_name", length = 150)
    private String roleName;

    /** Role captured at the time of the meeting (e.g. UIDAI, PMC, MSP, Chair). */
    @Column(name = "participant_role", length = 100)
    private String participantRole;

    @Column(name = "mandatory", nullable = false)
    private boolean mandatory = false;

    /** True for external guests not held in the User service (not validated). */
    @Column(name = "is_external", nullable = false)
    private boolean external = false;

    /** True when this attendee was recorded as present (set when updating the meeting). */
    @Column(name = "is_present", nullable = false)
    private boolean present = false;

    protected MeetingParticipant() {
    }

    public MeetingParticipant(Meeting meeting, String userId, String email, String roleName,
                              String participantRole, boolean mandatory, boolean external,
                              boolean present) {
        this.meeting = meeting;
        this.userId = userId;
        this.email = email;
        this.roleName = roleName;
        this.participantRole = participantRole;
        this.mandatory = mandatory;
        this.external = external;
        this.present = present;
    }

    public Long getId() {
        return id;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getParticipantRole() {
        return participantRole;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public boolean isExternal() {
        return external;
    }

    public boolean isPresent() {
        return present;
    }
}
