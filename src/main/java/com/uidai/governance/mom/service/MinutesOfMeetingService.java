package com.uidai.governance.mom.service;

import com.uidai.governance.common.audit.AuditAction;
import com.uidai.governance.common.audit.AuditLogService;
import com.uidai.governance.common.exception.BusinessValidationException;
import com.uidai.governance.common.exception.ResourceNotFoundException;
import com.uidai.governance.external.user.UserServiceClient;
import com.uidai.governance.external.user.dto.RoleValidationResult;
import com.uidai.governance.meeting.domain.Meeting;
import com.uidai.governance.meeting.service.MeetingService;
import com.uidai.governance.mom.domain.ActionItem;
import com.uidai.governance.mom.domain.Decision;
import com.uidai.governance.mom.domain.MinutesOfMeeting;
import com.uidai.governance.mom.domain.MoMStatus;
import com.uidai.governance.mom.domain.MoMTemplate;
import com.uidai.governance.mom.domain.Risk;
import com.uidai.governance.mom.dto.ActionItemInput;
import com.uidai.governance.mom.dto.CreateMoMRequest;
import com.uidai.governance.mom.dto.DecisionDto;
import com.uidai.governance.mom.dto.MoMResponse;
import com.uidai.governance.mom.dto.RiskDto;
import com.uidai.governance.mom.dto.UpdateMoMRequest;
import com.uidai.governance.mom.repository.MinutesOfMeetingRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recording and lifecycle of Minutes of Meeting (MEET-FR-04, MEET-FR-05).
 */
@Service
public class MinutesOfMeetingService {

    static final String ENTITY = "MinutesOfMeeting";

    private final MinutesOfMeetingRepository repository;
    private final MeetingService meetingService;
    private final MoMTemplateService templateService;
    private final UserServiceClient userServiceClient;
    private final ActivityLinkValidator activityLinkValidator;
    private final AuditLogService auditLogService;

    public MinutesOfMeetingService(MinutesOfMeetingRepository repository,
                                   MeetingService meetingService,
                                   MoMTemplateService templateService,
                                   UserServiceClient userServiceClient,
                                   ActivityLinkValidator activityLinkValidator,
                                   AuditLogService auditLogService) {
        this.repository = repository;
        this.meetingService = meetingService;
        this.templateService = templateService;
        this.userServiceClient = userServiceClient;
        this.activityLinkValidator = activityLinkValidator;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public MoMResponse createForMeeting(UUID meetingId, CreateMoMRequest request) {
        Meeting meeting = meetingService.getEntity(meetingId);
        if (repository.existsByMeetingId(meetingId)) {
            throw new BusinessValidationException("A MoM already exists for meeting " + meetingId);
        }
        MoMTemplate template = request.templateId() != null
                ? templateService.requireById(request.templateId()) : null;

        MinutesOfMeeting mom = new MinutesOfMeeting(meeting, template, request.title(), request.content());
        request.decisions().forEach(d -> mom.addDecision(new Decision(d.description(), d.ownerUserId())));
        request.risks().forEach(r -> mom.addRisk(
                new Risk(r.description(), r.severity(), r.mitigation(), r.ownerUserId())));
        for (ActionItemInput input : request.actionItems()) {
            validateAssignee(input.assignedToUserId());
            activityLinkValidator.validateExists(input.linkedActivityId());
            mom.addActionItem(toActionItem(input));
        }

        MinutesOfMeeting saved = repository.save(mom);
        auditLogService.record(AuditAction.MOM_CREATED, ENTITY, saved.getId(),
                "MoM '%s' created for meeting %s".formatted(saved.getTitle(), meetingId));
        return MoMResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public MoMResponse get(Long momId) {
        return MoMResponse.from(getEntity(momId));
    }

    @Transactional(readOnly = true)
    public MoMResponse getByMeeting(UUID meetingId) {
        return repository.findByMeetingId(meetingId)
                .map(MoMResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MoM for meeting " + meetingId + " was not found"));
    }

    @Transactional
    public MoMResponse update(Long momId, UpdateMoMRequest request) {
        MinutesOfMeeting mom = getEntity(momId);
        requireEditable(mom);
        MoMTemplate template = request.templateId() != null
                ? templateService.requireById(request.templateId()) : null;
        mom.setTitle(request.title());
        mom.setContent(request.content());
        mom.setTemplate(template);
        MinutesOfMeeting saved = repository.save(mom);
        auditLogService.record(AuditAction.MOM_UPDATED, ENTITY, saved.getId(), "MoM updated");
        return MoMResponse.from(saved);
    }

    @Transactional
    public MoMResponse changeStatus(Long momId, MoMStatus target) {
        MinutesOfMeeting mom = getEntity(momId);
        if (!mom.getStatus().canTransitionTo(target)) {
            throw new BusinessValidationException(
                    "Illegal MoM status transition %s -> %s".formatted(mom.getStatus(), target));
        }
        MoMStatus previous = mom.getStatus();
        mom.setStatus(target);
        MinutesOfMeeting saved = repository.save(mom);
        AuditAction action = target == MoMStatus.FINALIZED ? AuditAction.MOM_FINALIZED : AuditAction.MOM_UPDATED;
        auditLogService.record(action, ENTITY, saved.getId(),
                "MoM status %s -> %s".formatted(previous, target));
        return MoMResponse.from(saved);
    }

    /** Free-text MoM search (MEET-FR-05.2). A null/blank term returns all MoMs. */
    @Transactional(readOnly = true)
    public Page<MoMResponse> search(String term, Pageable pageable) {
        String normalized = (term == null || term.isBlank()) ? null : term.trim();
        Page<MinutesOfMeeting> page = (normalized == null)
                ? repository.findAll(pageable)
                : repository.search(normalized, pageable);
        return page.map(MoMResponse::from);
    }

    @Transactional(readOnly = true)
    public MinutesOfMeeting getEntity(Long momId) {
        return repository.findById(momId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY, momId));
    }

    private ActionItem toActionItem(ActionItemInput input) {
        ActionItem item = new ActionItem(input.description(), input.assignedToUserId(), input.dueDate());
        item.setLinkedTaskId(input.linkedTaskId());
        item.setLinkedMilestoneId(input.linkedMilestoneId());
        item.setLinkedTicketId(input.linkedTicketId());
        item.setLinkedActivityId(input.linkedActivityId());
        return item;
    }

    private void validateAssignee(String userId) {
        RoleValidationResult result =
                userServiceClient.validateParticipants(List.of(userId), "ACTION_ITEM_ASSIGNEE");
        if (!result.valid()) {
            throw new BusinessValidationException("Assignee is not an authorized user: " + userId);
        }
    }

    private void requireEditable(MinutesOfMeeting mom) {
        if (!mom.getStatus().isEditable()) {
            throw new BusinessValidationException(
                    "MoM is FINALIZED and part of the official record; it cannot be edited");
        }
    }
}
