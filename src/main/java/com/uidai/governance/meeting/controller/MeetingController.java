package com.uidai.governance.meeting.controller;

import com.uidai.governance.common.dto.PageResponse;
import com.uidai.governance.common.exception.BusinessValidationException;
import com.uidai.governance.meeting.domain.MeetingStatus;
import com.uidai.governance.meeting.dto.CreateMeetingRequest;
import com.uidai.governance.meeting.dto.MeetingResponse;
import com.uidai.governance.meeting.dto.MeetingSummary;
import com.uidai.governance.meeting.dto.UpdateMeetingRequest;
import com.uidai.governance.meeting.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
 * Meeting capture &amp; classification API (MEET-FR-01 .. MEET-FR-03).
 */
@RestController
@RequestMapping("/meetings")
@Tag(name = "Meetings", description = "Capture, classify and report on governance meetings")
public class MeetingController {

    private final MeetingService service;

    public MeetingController(MeetingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record a meeting (MEET-FR-01.1) with type, date, participants and agenda")
    public MeetingResponse create(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                  @Valid @RequestBody CreateMeetingRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a meeting by id")
    public MeetingResponse get(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                               @PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a meeting")
    public MeetingResponse update(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                  @PathVariable Long id, @Valid @RequestBody UpdateMeetingRequest request) {
        return service.update(id, request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Transition a meeting's status")
    public MeetingResponse changeStatus(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                        @PathVariable Long id, @RequestParam MeetingStatus status) {
        return service.changeStatus(id, status);
    }

    @GetMapping
    @Operation(summary = "Filter & report meetings by project, status and date (MEET-FR-02.3). "
            + "Pass 'ALL' (or omit) for projectId/status to skip that filter; from/to are optional.")
    public PageResponse<MeetingSummary> search(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "meetingDate"));
        return PageResponse.from(
                service.search(anyIfAll(projectId), parseStatus(status), from, to, pageable));
    }

    /** Treats a blank value or the literal {@code ALL} (any case) as "no filter". */
    private static String anyIfAll(String value) {
        return value == null || value.isBlank() || "ALL".equalsIgnoreCase(value.trim()) ? null : value;
    }

    /** Parses the status filter; {@code ALL}/blank means no filter, anything else must be a valid status. */
    private static MeetingStatus parseStatus(String status) {
        String value = anyIfAll(status);
        if (value == null) {
            return null;
        }
        try {
            return MeetingStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessValidationException("Unknown meeting status: " + status);
        }
    }
}
