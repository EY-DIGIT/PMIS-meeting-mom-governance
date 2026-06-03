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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A project-related meeting record (MEET-FR-01).
 *
 * <p>Carries the meeting's title, date and start/end time, an optional
 * description and meeting link, the linked project and its attendees. When a
 * meeting is recorded, a corresponding project activity is created in the
 * external project service and its identifiers are stored here.</p>
 */
@Entity
@Table(name = "meeting", indexes = {
        @Index(name = "idx_meeting_project", columnList = "project_id"),
        @Index(name = "idx_meeting_date", columnList = "meeting_date")
})
public class Meeting extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 250)
    private String title;

    /** Calendar date of the meeting. */
    @Column(name = "meeting_date", nullable = false)
    private LocalDate meetingDate;

    /** Start clock time of the meeting (combined with the date in IST for the activity). */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /** End clock time of the meeting. */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /** Free-text meeting description, reused as the linked activity description. */
    @Column(name = "description", length = 5000)
    private String description;

    /** Optional joining link (e.g. video-conference URL). */
    @Column(name = "meeting_link", length = 1000)
    private String meetingLink;

    /** Link to the project this meeting belongs to. */
    @Column(name = "project_id", nullable = false, length = 64)
    private String projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MeetingStatus status = MeetingStatus.DRAFT;

    /** Id of the project activity created for this meeting in the external project service. */
    @Column(name = "activity_id", length = 64)
    private String activityId;

    /** Project id of the linked activity (UUID from the external project service). */
    @Column(name = "activity_project_id", length = 64)
    private String activityProjectId;

    /** Milestone id the linked activity was created under (UUID from the external service). */
    @Column(name = "activity_milestone_id", length = 64)
    private String activityMilestoneId;

    /** Name of the linked activity as recorded in the external service. */
    @Column(name = "activity_name", length = 250)
    private String activityName;

    /** Description of the linked activity as recorded in the external service. */
    @Column(name = "activity_description", length = 5000)
    private String activityDescription;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingParticipant> participants = new ArrayList<>();

    protected Meeting() {
    }

    public Meeting(String title, LocalDate meetingDate, LocalTime startTime, LocalTime endTime,
                   String description, String meetingLink, String projectId) {
        this.title = title;
        this.meetingDate = meetingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.meetingLink = meetingLink;
        this.projectId = projectId;
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

    public LocalDate getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(LocalDate meetingDate) {
        this.meetingDate = meetingDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
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

    /** Records the identifiers of the project activity created for this meeting. */
    public void linkActivity(String activityId, String activityProjectId, String activityMilestoneId,
                             String activityName, String activityDescription) {
        this.activityId = activityId;
        this.activityProjectId = activityProjectId;
        this.activityMilestoneId = activityMilestoneId;
        this.activityName = activityName;
        this.activityDescription = activityDescription;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getActivityProjectId() {
        return activityProjectId;
    }

    public String getActivityMilestoneId() {
        return activityMilestoneId;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getActivityDescription() {
        return activityDescription;
    }
}
