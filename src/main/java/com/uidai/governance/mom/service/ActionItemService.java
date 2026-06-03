package com.uidai.governance.mom.service;

import com.uidai.governance.common.audit.AuditAction;
import com.uidai.governance.common.audit.AuditLogService;
import com.uidai.governance.common.exception.BusinessValidationException;
import com.uidai.governance.common.exception.ResourceNotFoundException;
import com.uidai.governance.external.user.UserServiceClient;
import com.uidai.governance.external.user.dto.RoleValidationResult;
import com.uidai.governance.mom.domain.ActionItem;
import com.uidai.governance.mom.domain.ActionItemComment;
import com.uidai.governance.mom.domain.ActionItemStatus;
import com.uidai.governance.mom.domain.ExtensionRequestStatus;
import com.uidai.governance.mom.domain.MinutesOfMeeting;
import com.uidai.governance.mom.domain.TimelineExtensionRequest;
import com.uidai.governance.mom.dto.ActionItemInput;
import com.uidai.governance.mom.dto.ActionItemResponse;
import com.uidai.governance.mom.dto.AddCommentRequest;
import com.uidai.governance.mom.dto.CommentDto;
import com.uidai.governance.mom.dto.CreateExtensionRequest;
import com.uidai.governance.mom.dto.DecideExtensionRequest;
import com.uidai.governance.mom.dto.ExtensionRequestResponse;
import com.uidai.governance.mom.dto.UpdateActionItemRequest;
import com.uidai.governance.mom.repository.ActionItemCommentRepository;
import com.uidai.governance.mom.repository.ActionItemRepository;
import com.uidai.governance.mom.repository.TimelineExtensionRequestRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Action item tracking, threaded comments (MEET-FR-04.4) and the governed
 * timeline extension workflow (MEET-FR-04.5).
 */
@Service
public class ActionItemService {

    private static final String ENTITY = "ActionItem";

    private final ActionItemRepository actionItemRepository;
    private final ActionItemCommentRepository commentRepository;
    private final TimelineExtensionRequestRepository extensionRepository;
    private final MinutesOfMeetingService momService;
    private final UserServiceClient userServiceClient;
    private final ActivityLinkValidator activityLinkValidator;
    private final AuditLogService auditLogService;

    public ActionItemService(ActionItemRepository actionItemRepository,
                             ActionItemCommentRepository commentRepository,
                             TimelineExtensionRequestRepository extensionRepository,
                             MinutesOfMeetingService momService,
                             UserServiceClient userServiceClient,
                             ActivityLinkValidator activityLinkValidator,
                             AuditLogService auditLogService) {
        this.actionItemRepository = actionItemRepository;
        this.commentRepository = commentRepository;
        this.extensionRepository = extensionRepository;
        this.momService = momService;
        this.userServiceClient = userServiceClient;
        this.activityLinkValidator = activityLinkValidator;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ActionItemResponse addToMom(Long momId, ActionItemInput input) {
        MinutesOfMeeting mom = momService.getEntity(momId);
        if (!mom.getStatus().isEditable()) {
            throw new BusinessValidationException("Cannot add action items to a FINALIZED MoM");
        }
        validateAssignee(input.assignedToUserId());
        activityLinkValidator.validateExists(input.linkedActivityId());
        ActionItem item = new ActionItem(input.description(), input.assignedToUserId(), input.dueDate());
        item.setLinkedTaskId(input.linkedTaskId());
        item.setLinkedMilestoneId(input.linkedMilestoneId());
        item.setLinkedTicketId(input.linkedTicketId());
        item.setLinkedActivityId(input.linkedActivityId());
        mom.addActionItem(item);
        ActionItem saved = actionItemRepository.save(item);
        auditLogService.record(AuditAction.ACTION_ITEM_CREATED, ENTITY, saved.getId(),
                "Action item created and assigned to " + saved.getAssignedToUserId());
        return ActionItemResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ActionItemResponse get(Long id) {
        return ActionItemResponse.from(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<ActionItemResponse> listForMom(Long momId) {
        return actionItemRepository.findByMomId(momId).stream().map(ActionItemResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<ActionItemResponse> listByAssignee(String userId, Pageable pageable) {
        return actionItemRepository.findByAssignedToUserId(userId, pageable).map(ActionItemResponse::from);
    }

    @Transactional
    public ActionItemResponse update(Long id, UpdateActionItemRequest request) {
        ActionItem item = getEntity(id);
        validateAssignee(request.assignedToUserId());
        activityLinkValidator.validateExists(request.linkedActivityId());

        boolean newlyCompleted = request.status() == ActionItemStatus.COMPLETED
                && item.getStatus() != ActionItemStatus.COMPLETED;

        item.setDescription(request.description());
        item.setAssignedToUserId(request.assignedToUserId());
        item.setStatus(request.status());
        item.setLinkedTaskId(request.linkedTaskId());
        item.setLinkedMilestoneId(request.linkedMilestoneId());
        item.setLinkedTicketId(request.linkedTicketId());
        item.setLinkedActivityId(request.linkedActivityId());
        ActionItem saved = actionItemRepository.save(item);
        auditLogService.record(AuditAction.ACTION_ITEM_UPDATED, ENTITY, saved.getId(),
                "Action item updated (status=" + saved.getStatus() + ")");

        // On completion, sync the linked project activity (best-effort).
        if (newlyCompleted) {
            activityLinkValidator.syncCompleted(saved.getLinkedActivityId());
        }
        return ActionItemResponse.from(saved);
    }

    // --- Threaded comments (MEET-FR-04.4) ---

    @Transactional
    public CommentDto addComment(Long actionItemId, AddCommentRequest request) {
        ActionItem item = getEntity(actionItemId);
        ActionItemComment parent = null;
        if (request.parentCommentId() != null) {
            parent = commentRepository.findById(request.parentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", request.parentCommentId()));
            if (!parent.getActionItem().getId().equals(actionItemId)) {
                throw new BusinessValidationException("Parent comment belongs to a different action item");
            }
        }
        ActionItemComment comment = new ActionItemComment(
                request.authorUserId(), request.content(), parent, Instant.now());
        item.addComment(comment);
        ActionItemComment saved = commentRepository.save(comment);
        auditLogService.record(AuditAction.ACTION_ITEM_COMMENT_ADDED, ENTITY, item.getId(),
                "Comment added by " + saved.getAuthorUserId());
        return CommentDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> listComments(Long actionItemId) {
        getEntity(actionItemId); // existence check
        return commentRepository.findByActionItemIdOrderByCreatedAtAsc(actionItemId)
                .stream().map(CommentDto::from).toList();
    }

    // --- Timeline extension workflow (MEET-FR-04.5) ---

    @Transactional
    public ExtensionRequestResponse requestExtension(Long actionItemId, CreateExtensionRequest request) {
        ActionItem item = getEntity(actionItemId);
        if (request.justification() == null || request.justification().isBlank()) {
            throw new BusinessValidationException("A documented justification is required (MEET-FR-04.5)");
        }
        TimelineExtensionRequest extension = new TimelineExtensionRequest(
                item.getDueDate(), request.requestedDueDate(), request.justification(),
                request.requestedByUserId(), Instant.now());
        item.addExtensionRequest(extension);
        TimelineExtensionRequest saved = extensionRepository.save(extension);
        auditLogService.record(AuditAction.EXTENSION_REQUESTED, ENTITY, item.getId(),
                "Extension requested to %s by %s. Justification: %s".formatted(
                        request.requestedDueDate(), request.requestedByUserId(), request.justification()));
        return ExtensionRequestResponse.from(saved);
    }

    @Transactional
    public ExtensionRequestResponse decideExtension(Long extensionId, DecideExtensionRequest request) {
        TimelineExtensionRequest extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new ResourceNotFoundException("ExtensionRequest", extensionId));
        if (extension.getStatus() != ExtensionRequestStatus.PENDING) {
            throw new BusinessValidationException("Extension request has already been decided");
        }
        // The approver must be a recognised authority in the User service.
        validateAuthority(request.decidedByUserId());

        ActionItem item = extension.getActionItem();
        if (request.approve()) {
            extension.decide(ExtensionRequestStatus.APPROVED, request.decidedByUserId(),
                    request.decisionComment(), Instant.now());
            item.setDueDate(extension.getRequestedDueDate());
            actionItemRepository.save(item);
            auditLogService.record(AuditAction.EXTENSION_APPROVED, ENTITY, item.getId(),
                    "Extension APPROVED by %s; new due date %s. Comment: %s".formatted(
                            request.decidedByUserId(), extension.getRequestedDueDate(),
                            request.decisionComment()));
        } else {
            extension.decide(ExtensionRequestStatus.REJECTED, request.decidedByUserId(),
                    request.decisionComment(), Instant.now());
            auditLogService.record(AuditAction.EXTENSION_REJECTED, ENTITY, item.getId(),
                    "Extension REJECTED by %s. Comment: %s".formatted(
                            request.decidedByUserId(), request.decisionComment()));
        }
        return ExtensionRequestResponse.from(extensionRepository.save(extension));
    }

    @Transactional(readOnly = true)
    public ActionItem getEntity(Long id) {
        return actionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY, id));
    }

    private void validateAssignee(String userId) {
        RoleValidationResult result =
                userServiceClient.validateParticipants(List.of(userId), "ACTION_ITEM_ASSIGNEE");
        if (!result.valid()) {
            throw new BusinessValidationException("Assignee is not an authorized user: " + userId);
        }
    }

    private void validateAuthority(String userId) {
        RoleValidationResult result =
                userServiceClient.validateParticipants(List.of(userId), "EXTENSION_APPROVER");
        if (!result.valid()) {
            throw new BusinessValidationException(
                    "User is not authorized to approve timeline extensions: " + userId);
        }
    }
}
