package com.uidai.governance.meeting.domain;

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
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A project-related meeting record (MEET-FR-01).
 *
 * <p>Linked to a project, stage and service provider (MEET-FR-01.2), classified
 * by a configurable {@link MeetingType} (MEET-FR-02), and carrying the mandatory
 * date, participants and agenda (MEET-FR-03). All records are auditable via the
 * inherited audit columns and the central audit log (MEET-FR-01.3).</p>
 */
@Entity
@Table(name = "meeting", indexes = {
        @Index(name = "idx_meeting_project", columnList = "project_id"),
        @Index(name = "idx_meeting_type", columnList = "meeting_type_id"),
        @Index(name = "idx_meeting_date", columnList = "meeting_date")
})
public class Meeting extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 250)
    private String title;

    @ManyToOne(optional = false)
    @JoinColumn(name = "meeting_type_id", nullable = false)
    private MeetingType meetingType;

    /** Scheduled date/time of the meeting (MEET-FR-03). */
    @Column(name = "meeting_date", nullable = false)
    private Instant meetingDate;

    /** Link to the project this meeting belongs to (MEET-FR-01.2). */
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /** Link to the project stage (MEET-FR-01.2). */
    @Column(name = "stage_id")
    private Long stageId;

    /** Link to the service provider / MSP (MEET-FR-01.2). */
    @Column(name = "service_provider_id")
    private Long serviceProviderId;

    /** Agenda, preserved for audit and traceability (MEET-FR-03.3). */
    @Column(name = "agenda", nullable = false, length = 5000)
    private String agenda;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MeetingStatus status = MeetingStatus.DRAFT;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingParticipant> participants = new ArrayList<>();

    protected Meeting() {
    }

    public Meeting(String title, MeetingType meetingType, Instant meetingDate, Long projectId,
                   Long stageId, Long serviceProviderId, String agenda) {
        this.title = title;
        this.meetingType = meetingType;
        this.meetingDate = meetingDate;
        this.projectId = projectId;
        this.stageId = stageId;
        this.serviceProviderId = serviceProviderId;
        this.agenda = agenda;
        this.status = MeetingStatus.DRAFT;
    }

    public void addParticipant(MeetingParticipant participant) {
        participant.setMeeting(this);
        this.participants.add(participant);
    }

    public void clearParticipants() {
        this.participants.clear();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public MeetingType getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(MeetingType meetingType) {
        this.meetingType = meetingType;
    }

    public Instant getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(Instant meetingDate) {
        this.meetingDate = meetingDate;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public Long getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(Long serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public String getAgenda() {
        return agenda;
    }

    public void setAgenda(String agenda) {
        this.agenda = agenda;
    }

    public MeetingStatus getStatus() {
        return status;
    }

    public void setStatus(MeetingStatus status) {
        this.status = status;
    }

    public List<MeetingParticipant> getParticipants() {
        return participants;
    }
}
