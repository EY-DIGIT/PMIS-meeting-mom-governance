package com.uidai.governance.common.audit;

import com.uidai.governance.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only access to the immutable audit trail (MEET-FR-01.3, MEET-FR-04.5).
 */
@RestController
@RequestMapping("/audit-logs")
@Tag(name = "Audit Log", description = "Immutable audit trail for meetings, MoMs and action items")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @Operation(summary = "List audit-trail entries for a given entity")
    public PageResponse<AuditLog> forEntity(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLog> result = auditLogService.forEntity(entityType, entityId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt")));
        return PageResponse.from(result);
    }
}
