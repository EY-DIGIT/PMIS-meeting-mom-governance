package com.uidai.governance.meeting.service;

import com.uidai.governance.common.audit.AuditAction;
import com.uidai.governance.common.audit.AuditLogService;
import com.uidai.governance.common.exception.BusinessValidationException;
import com.uidai.governance.common.exception.ResourceNotFoundException;
import com.uidai.governance.external.activity.ActivityServiceClient;
import com.uidai.governance.external.activity.dto.ActivityDto;
import com.uidai.governance.external.activity.dto.CreateActivityRequest;
import com.uidai.governance.external.user.UserServiceClient;
import com.uidai.governance.external.user.dto.RoleValidationResult;
import com.uidai.governance.meeting.domain.Meeting;
import com.uidai.governance.meeting.domain.MeetingParticipant;
import com.uidai.governance.meeting.domain.MeetingStatus;
import com.uidai.governance.meeting.dto.CreateMeetingRequest;
import com.uidai.governance.meeting.dto.ExternalAttendeeDto;
import com.uidai.governance.meeting.dto.MeetingResponse;
import com.uidai.governance.meeting.dto.MeetingSummary;
import com.uidai.governance.meeting.dto.ParticipantDto;
import com.uidai.governance.meeting.dto.UpdateMeetingRequest;
import com.uidai.governance.meeting.repository.MeetingRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core meeting capture logic (MEET-FR-01). Recording a meeting also creates a
 * linked project activity in the external project service, derived from the
 * meeting's own fields.
 */
@Service
public class MeetingService {

    private static final String ENTITY = "Meeting";
    /** Context label passed to participant validation (role rules are service-side). */
    private static final String PARTICIPANT_CONTEXT = "MEETING_PARTICIPANT";
    /** Default priority for the auto-created project activity. */
    private static final String ACTIVITY_PRIORITY = "P3";
    /** Meeting times are interpreted in IST when building the activity timestamps. */
    private static final ZoneId MEETING_ZONE = ZoneId.of("Asia/Kolkata");

    private final MeetingRepository meetingRepository;
    private final UserServiceClient userServiceClient;
    private final ActivityServiceClient activityServiceClient;
    private final AuditLogService auditLogService;

    public MeetingService(MeetingRepository meetingRepository,
                          UserServiceClient userServiceClient,
                          ActivityServiceClient activityServiceClient,
                          AuditLogService auditLogService) {
        this.meetingRepository = meetingRepository;
        this.userServiceClient = userServiceClient;
        this.activityServiceClient = activityServiceClient;
        this.auditLogService = auditLogService;
    }

    /** Records a meeting (MEET-FR-01.1) and creates its linked project activity. */
    @Transactional
    public MeetingResponse create(CreateMeetingRequest request) {
        Meeting meeting = new Meeting(request.title(), request.meetingDate(), request.startTime(),
                request.endTime(), request.description(), request.meetingLink(), request.projectId());
        applyAttendees(meeting, request.attendees(), request.externalAttendees());
        createAndLinkActivity(meeting, request.milestoneId(), request.attachments());

        Meeting saved = meetingRepository.save(meeting);
        auditLogService.record(AuditAction.MEETING_CREATED, ENTITY, saved.getId(),
                "Meeting '%s' created (project=%s)".formatted(saved.getTitle(), saved.getProjectId()));
        return MeetingResponse.from(saved);
    }

    /**
     * Creates a project activity for the meeting from the meeting's own fields and
     * stores the returned identifiers on it. No-op when the activity service is
     * disabled. A failure aborts meeting creation (same transaction).
     */
    private void createAndLinkActivity(Meeting meeting, String milestoneId, List<String> attachments) {
        if (!activityServiceClient.isEnabled()) {
            return;
        }
        OffsetDateTime startDate = meeting.getMeetingDate().atTime(meeting.getStartTime())
                .atZone(MEETING_ZONE).toOffsetDateTime();
        OffsetDateTime endDate = meeting.getMeetingDate().atTime(meeting.getEndTime())
                .atZone(MEETING_ZONE).toOffsetDateTime();
        CreateActivityRequest activityRequest = new CreateActivityRequest(
                meeting.getTitle(),
                meeting.getDescription(),
                startDate,
                endDate,
                null,                 // actualStartDate
                null,                 // actualEndDate
                null,                 // status
                null,                 // activityStarted
                ACTIVITY_PRIORITY,
                null,                 // position
                null,                 // ownerDivision
                null,                 // ownerDivisionOther
                List.of(),            // concernedDivision
                null,                 // concernedDivisionOther
                null,                 // vendorId
                List.of(),            // dependsOn
                null,                 // category
                null                // ccnValue
               );
        ActivityDto activity = activityServiceClient.createActivity(milestoneId, activityRequest);
        if (activity != null) {
            meeting.linkActivity(activity.id(), activity.projectId(), activity.milestoneId(),
                    activity.name(), activity.description());
        }
    }

    @Transactional
    public MeetingResponse update(Long id, UpdateMeetingRequest request) {
        Meeting meeting = getEntity(id);
        if (meeting.getStatus() == MeetingStatus.COMPLETED || meeting.getStatus() == MeetingStatus.CANCELLED) {
            throw new BusinessValidationException(
                    "Cannot edit a meeting in status " + meeting.getStatus());
        }
        meeting.setTitle(request.title());
        meeting.setMeetingDate(request.meetingDate());
        meeting.setStartTime(request.startTime());
        meeting.setEndTime(request.endTime());
        meeting.setDescription(request.description());
        meeting.setMeetingLink(request.meetingLink());
        meeting.setProjectId(request.projectId());
        // Remove the old attendees and flush the deletes before re-inserting, so
        // re-adding the same (meeting_id, user_id) does not trip the unique
        // constraint (Hibernate would otherwise order the inserts before the deletes).
        meeting.clearParticipants();
        meetingRepository.saveAndFlush(meeting);
        applyAttendees(meeting, request.attendees(), request.externalAttendees());

        Meeting saved = meetingRepository.save(meeting);
        auditLogService.record(AuditAction.MEETING_UPDATED, ENTITY, saved.getId(),
                "Meeting '%s' updated".formatted(saved.getTitle()));
        return MeetingResponse.from(saved);
    }

    @Transactional
    public MeetingResponse changeStatus(Long id, MeetingStatus target) {
        Meeting meeting = getEntity(id);
        if (!meeting.getStatus().canTransitionTo(target)) {
            throw new BusinessValidationException(
                    "Illegal status transition %s -> %s".formatted(meeting.getStatus(), target));
        }
        MeetingStatus previous = meeting.getStatus();
        meeting.setStatus(target);
        Meeting saved = meetingRepository.save(meeting);
        auditLogService.record(AuditAction.MEETING_STATUS_CHANGED, ENTITY, saved.getId(),
                "Status changed %s -> %s".formatted(previous, target));
        return MeetingResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public MeetingResponse get(Long id) {
        return MeetingResponse.from(getEntity(id));
    }

    /** Filter &amp; report query across project, status and date (MEET-FR-02.3). */
    @Transactional(readOnly = true)
    public Page<MeetingSummary> search(String projectId, MeetingStatus status,
                                       LocalDate from, LocalDate to, Pageable pageable) {
        Specification<Meeting> spec = Specification.allOf(
                MeetingSpecifications.project(projectId),
                MeetingSpecifications.status(status),
                MeetingSpecifications.from(from),
                MeetingSpecifications.to(to));
        return meetingRepository.findAll(spec, pageable).map(MeetingSummary::from);
    }

    @Transactional(readOnly = true)
    public Meeting getEntity(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY, id));
    }

    /**
     * Validates internal attendees against the User service and applies both
     * internal and external attendees to the meeting. External attendees are
     * stored as-is and are not validated. Attendee ids must be unique across both
     * lists (a single {@code (meeting, user_id)} per the table constraint).
     */
    private void applyAttendees(Meeting meeting, List<ParticipantDto> attendees,
                                List<ExternalAttendeeDto> externalAttendees) {
        List<ParticipantDto> internal = attendees == null ? List.of() : attendees;
        List<ExternalAttendeeDto> external = externalAttendees == null ? List.of() : externalAttendees;

        List<String> allIds = new ArrayList<>(internal.size() + external.size());
        internal.forEach(p -> allIds.add(p.userId()));
        external.forEach(e -> allIds.add(e.email()));
        if (allIds.stream().distinct().count() != allIds.size()) {
            throw new BusinessValidationException("Duplicate attendee detected");
        }

        validateInternalAttendees(internal);

        for (ParticipantDto p : internal) {
            meeting.addParticipant(new MeetingParticipant(meeting, p.userId(), p.participantRole(),
                    p.mandatory(), false, p.isPresent()));
        }
        for (ExternalAttendeeDto e : external) {
            meeting.addParticipant(new MeetingParticipant(meeting, e.email(), null, false, true, e.isPresent()));
        }
    }

    private void validateInternalAttendees(List<ParticipantDto> internal) {
        if (internal.isEmpty()) {
            return;
        }
        List<String> userIds = internal.stream().map(ParticipantDto::userId).toList();
        RoleValidationResult result = userServiceClient.validateParticipants(userIds, PARTICIPANT_CONTEXT);
        if (!result.valid()) {
            throw new BusinessValidationException(
                    "Attendees are not valid / active users: " + result.invalidUserIds());
        }
    }
}
