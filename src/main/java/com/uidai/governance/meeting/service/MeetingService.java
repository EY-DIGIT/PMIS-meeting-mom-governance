package com.uidai.governance.meeting.service;

import com.uidai.governance.common.audit.AuditAction;
import com.uidai.governance.common.audit.AuditLogService;
import com.uidai.governance.common.exception.BusinessValidationException;
import com.uidai.governance.common.exception.ResourceNotFoundException;
import com.uidai.governance.external.user.UserServiceClient;
import com.uidai.governance.external.user.dto.RoleValidationResult;
import com.uidai.governance.meeting.domain.Meeting;
import com.uidai.governance.meeting.domain.MeetingParticipant;
import com.uidai.governance.meeting.domain.MeetingStatus;
import com.uidai.governance.meeting.domain.MeetingType;
import com.uidai.governance.meeting.dto.CreateMeetingRequest;
import com.uidai.governance.meeting.dto.MeetingResponse;
import com.uidai.governance.meeting.dto.MeetingSummary;
import com.uidai.governance.meeting.dto.ParticipantDto;
import com.uidai.governance.meeting.dto.UpdateMeetingRequest;
import com.uidai.governance.meeting.repository.MeetingRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core meeting capture &amp; classification logic (MEET-FR-01 .. MEET-FR-03).
 */
@Service
public class MeetingService {

    private static final String ENTITY = "Meeting";

    private final MeetingRepository meetingRepository;
    private final MeetingTypeService meetingTypeService;
    private final UserServiceClient userServiceClient;
    private final AuditLogService auditLogService;

    public MeetingService(MeetingRepository meetingRepository,
                          MeetingTypeService meetingTypeService,
                          UserServiceClient userServiceClient,
                          AuditLogService auditLogService) {
        this.meetingRepository = meetingRepository;
        this.meetingTypeService = meetingTypeService;
        this.userServiceClient = userServiceClient;
        this.auditLogService = auditLogService;
    }

    /** Records a meeting (MEET-FR-01.1) with classification and mandatory fields. */
    @Transactional
    public MeetingResponse create(CreateMeetingRequest request) {
        MeetingType type = meetingTypeService.requireActiveByCode(request.meetingTypeCode());
        validateParticipants(request.participants(), type.getCode());

        Meeting meeting = new Meeting(request.title(), type, request.meetingDate(),
                request.projectId(), request.stageId(), request.serviceProviderId(), request.agenda());
        applyParticipants(meeting, request.participants());

        Meeting saved = meetingRepository.save(meeting);
        auditLogService.record(AuditAction.MEETING_CREATED, ENTITY, saved.getId(),
                "Meeting '%s' created (type=%s, project=%d)".formatted(
                        saved.getTitle(), type.getCode(), saved.getProjectId()));
        return MeetingResponse.from(saved);
    }

    @Transactional
    public MeetingResponse update(Long id, UpdateMeetingRequest request) {
        Meeting meeting = getEntity(id);
        if (meeting.getStatus() == MeetingStatus.COMPLETED || meeting.getStatus() == MeetingStatus.CANCELLED) {
            throw new BusinessValidationException(
                    "Cannot edit a meeting in status " + meeting.getStatus());
        }
        MeetingType type = meetingTypeService.requireActiveByCode(request.meetingTypeCode());
        validateParticipants(request.participants(), type.getCode());

        meeting.setTitle(request.title());
        meeting.setMeetingType(type);
        meeting.setMeetingDate(request.meetingDate());
        meeting.setProjectId(request.projectId());
        meeting.setStageId(request.stageId());
        meeting.setServiceProviderId(request.serviceProviderId());
        meeting.setAgenda(request.agenda());
        meeting.clearParticipants();
        applyParticipants(meeting, request.participants());

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

    /** Filter &amp; report query across category, project, provider, status and date. */
    @Transactional(readOnly = true)
    public Page<MeetingSummary> search(String typeCode, Long projectId, Long serviceProviderId,
                                       MeetingStatus status, Instant from, Instant to, Pageable pageable) {
        Specification<Meeting> spec = Specification.allOf(
                MeetingSpecifications.typeCode(typeCode),
                MeetingSpecifications.project(projectId),
                MeetingSpecifications.serviceProvider(serviceProviderId),
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

    private void validateParticipants(List<ParticipantDto> participants, String meetingTypeCode) {
        List<String> userIds = participants.stream().map(ParticipantDto::userId).distinct().toList();
        if (userIds.size() != participants.size()) {
            throw new BusinessValidationException("Duplicate participant detected");
        }
        RoleValidationResult result = userServiceClient.validateParticipants(userIds, meetingTypeCode);
        if (!result.valid()) {
            throw new BusinessValidationException(
                    "Participants not authorized for this meeting type: " + result.invalidUserIds());
        }
    }

    private void applyParticipants(Meeting meeting, List<ParticipantDto> participants) {
        for (ParticipantDto p : participants) {
            meeting.addParticipant(new MeetingParticipant(meeting, p.userId(),
                    p.participantRole(), p.mandatory()));
        }
    }
}
