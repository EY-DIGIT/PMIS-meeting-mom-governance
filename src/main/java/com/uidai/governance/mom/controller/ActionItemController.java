package com.uidai.governance.mom.controller;

import com.uidai.governance.common.dto.PageResponse;
import com.uidai.governance.mom.dto.ActionItemResponse;
import com.uidai.governance.mom.dto.AddCommentRequest;
import com.uidai.governance.mom.dto.CommentDto;
import com.uidai.governance.mom.dto.CreateExtensionRequest;
import com.uidai.governance.mom.dto.DecideExtensionRequest;
import com.uidai.governance.mom.dto.ExtensionRequestResponse;
import com.uidai.governance.mom.dto.UpdateActionItemRequest;
import com.uidai.governance.mom.service.ActionItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Action item tracking, threaded comments (MEET-FR-04.4) and the timeline
 * extension workflow (MEET-FR-04.5).
 */
@RestController
@RequestMapping("/action-items")
@Tag(name = "Action Items", description = "Track action items, comment threads and timeline extensions")
public class ActionItemController {

    private final ActionItemService service;

    public ActionItemController(ActionItemService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an action item")
    public ActionItemResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an action item")
    public ActionItemResponse update(@PathVariable Long id, @Valid @RequestBody UpdateActionItemRequest request) {
        return service.update(id, request);
    }

    @GetMapping
    @Operation(summary = "List action items assigned to a user")
    public PageResponse<ActionItemResponse> listByAssignee(
            @RequestParam String assignee,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "dueDate"));
        return PageResponse.from(service.listByAssignee(assignee, pageable));
    }

    // --- Threaded comments (MEET-FR-04.4) ---

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a comment (optionally a reply) to an action item")
    public CommentDto addComment(@PathVariable Long id, @Valid @RequestBody AddCommentRequest request) {
        return service.addComment(id, request);
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "List an action item's threaded comment history")
    public List<CommentDto> listComments(@PathVariable Long id) {
        return service.listComments(id);
    }

    // --- Timeline extension workflow (MEET-FR-04.5) ---

    @PostMapping("/{id}/extensions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Request a timeline extension with documented justification")
    public ExtensionRequestResponse requestExtension(@PathVariable Long id,
                                                      @Valid @RequestBody CreateExtensionRequest request) {
        return service.requestExtension(id, request);
    }

    @PostMapping("/extensions/{extensionId}/decision")
    @Operation(summary = "Approve or reject a timeline extension (designated authority)")
    public ExtensionRequestResponse decideExtension(@PathVariable Long extensionId,
                                                    @Valid @RequestBody DecideExtensionRequest request) {
        return service.decideExtension(extensionId, request);
    }
}
