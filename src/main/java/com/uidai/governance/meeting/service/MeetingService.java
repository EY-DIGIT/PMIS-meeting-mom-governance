package com.uidai.governance.meeting.service;

import com.uidai.governance.common.audit.AuditAction;
import com.uidai.governance.common.audit.AuditLogService;
import com.uidai.governance.common.exception.BusinessValidationException;
import com.uidai.governance.common.exception.ResourceNotFoundException;
import com.uidai.governance.external.activity.ActivityServiceClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.uidai.governance.external.activity.dto.ActivityDto;
import com.uidai.governance.external.activity.dto.AttachmentPayload;
import com.uidai.governance.external.activity.dto.CreateActivityRequest;
import com.uidai.governance.external.activity.dto.ProjectDto;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(MeetingService.class);

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
        meeting.setMeetingCode("MEET." + meetingRepository.nextMeetingCodeSeq());
        applyAttendees(meeting, request.attendees(), request.externalAttendees());
        ProjectDto project = resolveProject(meeting.getProjectId());
        applyProjectInfo(meeting, project);
        createAndLinkActivity(meeting, project, request.attachments());

        Meeting saved = meetingRepository.save(meeting);
        auditLogService.record(AuditAction.MEETING_CREATED, ENTITY, saved.getId(),
                "Meeting '%s' created (project=%s)".formatted(saved.getTitle(), saved.getProjectId()));
        return MeetingResponse.from(saved);
    }

    /** Looks up the project (for its code/name/milestone); null when the activity service is off. */
    private ProjectDto resolveProject(String projectId) {
        if (!activityServiceClient.isEnabled()) {
            return null;
        }
        return activityServiceClient.getProject(projectId).orElse(null);
    }

    /** Stores the project's display code and name on the meeting (when resolved). */
    private void applyProjectInfo(Meeting meeting, ProjectDto project) {
        if (project != null) {
            meeting.setProjectCode(project.projectCode());
            meeting.setProjectName(project.name());
        }
    }

    /**
     * Creates a project activity for the meeting from the meeting's own fields and
     * stores the returned identifiers on it, under the project's meetingMilestoneId.
     * No-op when the activity service is disabled. A failure aborts the transaction.
     */
    private void createAndLinkActivity(Meeting meeting, ProjectDto project, List<AttachmentPayload> attachments) {
        if (!activityServiceClient.isEnabled()) {
            return;
        }
        String milestoneId = project == null ? null : project.meetingMilestoneId();
        if (milestoneId == null || milestoneId.isBlank()) {
            throw new BusinessValidationException(
                    "No meetingMilestoneId found for project " + meeting.getProjectId());
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
                List.of(),            // concernedDivision
                null,                 // vendorId
                List.of(),            // dependsOn
                null,                 // category
                null,                 // ccnValue
                attachments);         // omitted from the payload when null/empty
        ActivityDto activity = activityServiceClient.createActivity(milestoneId, activityRequest);
        if (activity != null) {
            meeting.linkActivity(activity.id(), activity.projectId(), activity.milestoneId(),
                    activity.name(), activity.description());
            meeting.setAttachments(resolveActivityAttachments(activity, attachments));
        }
    }

    /**
     * Attachments to store on the meeting: those echoed by the activity-create
     * response, or - if files were sent but the create response did not echo them -
     * fetched from {@code GET /activities/{id}}. Best-effort: a failure to fetch
     * leaves attachments unset rather than aborting the (already created) activity.
     */
    private JsonNode resolveActivityAttachments(ActivityDto activity, List<AttachmentPayload> sent) {
        JsonNode stored = attachmentsOf(activity);
        boolean hasStored = stored != null && !stored.isEmpty();
        boolean sentFiles = sent != null && !sent.isEmpty();
        if (!hasStored && sentFiles && activity.id() != null) {
            try {
                stored = activityServiceClient.getActivity(activity.id())
                        .map(this::attachmentsOf).orElse(null);
            } catch (RuntimeException ex) {
                log.warn("Could not fetch attachments for activity {}: {}", activity.id(), ex.getMessage());
            }
        }
        return stored;
    }

    /**
     * Extracts the attachment array from an activity. PMIS nests it under the
     * activity's comment ({@code data.comment.attachments}); a top-level
     * {@code attachments} node is used as a fallback if present.
     */
    private JsonNode attachmentsOf(ActivityDto activity) {
        if (activity == null) {
            return null;
        }
        if (activity.attachments() != null && !activity.attachments().isEmpty()) {
            return activity.attachments();
        }
        JsonNode comment = activity.comment();
        if (comment != null) {
            JsonNode att = comment.get("attachments");
            if (att != null && !att.isNull()) {
                return att;
            }
        }
        return null;
    }

    @Transactional
    public MeetingResponse update(UUID id, UpdateMeetingRequest request) {
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
        applyProjectInfo(meeting, resolveProject(request.projectId()));
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
    public MeetingResponse changeStatus(UUID id, MeetingStatus target) {
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

    @Transactional
    public MeetingResponse get(UUID id) {
        Meeting meeting = getEntity(id);
        backfillProjectInfo(List.of(meeting));
        return MeetingResponse.from(meeting);
    }

    /** Filter &amp; report query across project, status and date (MEET-FR-02.3). */
    @Transactional
    public Page<MeetingSummary> search(String projectId, MeetingStatus status,
                                       LocalDate from, LocalDate to, Pageable pageable) {
        Specification<Meeting> spec = Specification.allOf(
                MeetingSpecifications.project(projectId),
                MeetingSpecifications.status(status),
                MeetingSpecifications.from(from),
                MeetingSpecifications.to(to));
        Page<Meeting> page = meetingRepository.findAll(spec, pageable);
        backfillProjectInfo(page.getContent());
        return page.map(MeetingSummary::from);
    }

    /**
     * Fills in project code/name for meetings missing them by resolving the project
     * once per distinct id. Best-effort: a project-service failure is logged and
     * leaves the values null rather than failing the read. Resolved values are
     * persisted (these methods run in a writable transaction).
     */
    private void backfillProjectInfo(List<Meeting> meetings) {
        if (!activityServiceClient.isEnabled()) {
            return;
        }
        Map<String, ProjectDto> byProject = new HashMap<>();
        for (Meeting meeting : meetings) {
            if (meeting.getProjectCode() != null && meeting.getProjectName() != null) {
                continue;
            }
            if (!byProject.containsKey(meeting.getProjectId())) {
                byProject.put(meeting.getProjectId(), safeResolveProject(meeting.getProjectId()));
            }
            applyProjectInfo(meeting, byProject.get(meeting.getProjectId()));
        }
    }

    /** Resolves a project, swallowing errors so reads never fail on a project-service issue. */
    private ProjectDto safeResolveProject(String projectId) {
        try {
            return resolveProject(projectId);
        } catch (RuntimeException ex) {
            log.warn("Could not resolve project {} for code/name backfill: {}", projectId, ex.getMessage());
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Meeting getEntity(UUID id) {
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
