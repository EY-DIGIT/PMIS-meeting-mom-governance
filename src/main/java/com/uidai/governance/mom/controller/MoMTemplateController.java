package com.uidai.governance.mom.controller;

import com.uidai.governance.mom.dto.MoMTemplateDto;
import com.uidai.governance.mom.service.MoMTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administration of standardized MoM templates (MEET-FR-05.1).
 */
@RestController
@RequestMapping("/mom-templates")
@Tag(name = "MoM Templates", description = "Standardized Minutes-of-Meeting templates")
public class MoMTemplateController {

    private final MoMTemplateService service;

    public MoMTemplateController(MoMTemplateService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List active MoM templates")
    public List<MoMTemplateDto> list() {
        return service.listActive();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a standardized MoM template")
    public MoMTemplateDto create(@Valid @RequestBody MoMTemplateDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a MoM template")
    public MoMTemplateDto update(@PathVariable Long id, @Valid @RequestBody MoMTemplateDto dto) {
        return service.update(id, dto);
    }
}
