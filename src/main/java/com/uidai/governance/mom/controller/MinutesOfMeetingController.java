package com.uidai.governance.mom.controller;

import com.uidai.governance.common.dto.PageResponse;
import com.uidai.governance.mom.domain.MoMStatus;
import com.uidai.governance.mom.dto.ActionItemInput;
import com.uidai.governance.mom.dto.ActionItemResponse;
import com.uidai.governance.mom.dto.CreateMoMRequest;
import com.uidai.governance.mom.dto.MoMResponse;
import com.uidai.governance.mom.dto.UpdateMoMRequest;
import com.uidai.governance.mom.service.ActionItemService;
import com.uidai.governance.mom.service.MinutesOfMeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minutes of Meeting API (MEET-FR-04, MEET-FR-05). Served under
 * {@code /meetings/mom} (context-path + {@code /mom}).
 */
@RestController
@RequestMapping("/mom")
@Tag(name = "Minutes of Meeting", description = "Record, structure, finalize and search MoMs. "
        + "All operations require an Authorization: Bearer token.")
public class MinutesOfMeetingController {

    private final MinutesOfMeetingService momService;
    private final ActionItemService actionItemService;

    public MinutesOfMeetingController(MinutesOfMeetingService momService,
                                      ActionItemService actionItemService) {
        this.momService = momService;
        this.actionItemService = actionItemService;
    }

    @PostMapping("/create/{meetingId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record the MoM for a meeting, structured into decisions, actions and risks")
    public MoMResponse create(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                              @PathVariable UUID meetingId, @Valid @RequestBody CreateMoMRequest request) {
        return momService.createForMeeting(meetingId, request);
    }

    @GetMapping("/getByMeeting/{meetingId}")
    @Operation(summary = "Get the MoM for a meeting")
    public MoMResponse getByMeeting(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                    @PathVariable UUID meetingId) {
        return momService.getByMeeting(meetingId);
    }

    @GetMapping("/get/{momId}")
    @Operation(summary = "Get a MoM by id")
    public MoMResponse get(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                           @PathVariable Long momId) {
        return momService.get(momId);
    }

    @PutMapping("/update/{momId}")
    @Operation(summary = "Update a MoM (only while not FINALIZED)")
    public MoMResponse update(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                              @PathVariable Long momId, @Valid @RequestBody UpdateMoMRequest request) {
        return momService.update(momId, request);
    }

    @PutMapping("/updateStatus/{momId}")
    @Operation(summary = "Transition MoM status (e.g. FINALIZE to add to the official record)")
    public MoMResponse changeStatus(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                    @PathVariable Long momId, @RequestParam MoMStatus status) {
        return momService.changeStatus(momId, status);
    }

    @GetMapping("/getAll")
    @Operation(summary = "Search MoMs by free text (MEET-FR-05.2)")
    public PageResponse<MoMResponse> search(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam(required = false) String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(momService.search(term, pageable));
    }

    @PostMapping("/addActionItem/{momId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an action item to a MoM")
    public ActionItemResponse addActionItem(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                            @PathVariable Long momId,
                                            @Valid @RequestBody ActionItemInput input) {
        return actionItemService.addToMom(momId, input);
    }

    @GetMapping("/getActionItems/{momId}")
    @Operation(summary = "List action items for a MoM")
    public List<ActionItemResponse> listActionItems(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                    @PathVariable Long momId) {
        return actionItemService.listForMom(momId);
    }
}
