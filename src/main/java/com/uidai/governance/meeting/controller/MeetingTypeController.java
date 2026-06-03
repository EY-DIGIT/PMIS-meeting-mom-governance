package com.uidai.governance.meeting.controller;

import com.uidai.governance.meeting.dto.MeetingTypeDto;
import com.uidai.governance.meeting.service.MeetingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
 * Administration of configurable meeting types (MEET-FR-02.4).
 */
@RestController
@RequestMapping("/meeting-types")
@Tag(name = "Meeting Types", description = "Configurable meeting categories (Steering, Governance, Migration, Ad-hoc)")
public class MeetingTypeController {

    private final MeetingTypeService service;

    public MeetingTypeController(MeetingTypeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List meeting types")
    public List<MeetingTypeDto> list(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                     @RequestParam(defaultValue = "true") boolean activeOnly) {
        return activeOnly ? service.listActive() : service.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new configurable meeting type (MEET-FR-02.4)")
    public MeetingTypeDto create(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                 @Valid @RequestBody MeetingTypeDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a meeting type")
    public MeetingTypeDto update(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                 @PathVariable Long id, @Valid @RequestBody MeetingTypeDto dto) {
        return service.update(id, dto);
    }
}
