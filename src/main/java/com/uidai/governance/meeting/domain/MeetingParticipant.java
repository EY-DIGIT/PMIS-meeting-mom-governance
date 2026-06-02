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
 * A participant of a meeting. The {@code userId} references a user in the
 * external Python User service and is validated against authorized roles before
 * persistence (MEET-FR-03.2).
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

    protected MeetingParticipant() {
    }

    public MeetingParticipant(Meeting meeting, String userId, String participantRole, boolean mandatory) {
        this.meeting = meeting;
        this.userId = userId;
        this.participantRole = participantRole;
        this.mandatory = mandatory;
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
}
