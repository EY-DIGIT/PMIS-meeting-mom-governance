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

    public MeetingParticipant(Meeting meeting, String userId, String participantRole,
                              boolean mandatory, boolean external, boolean present) {
        this.meeting = meeting;
        this.userId = userId;
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
