package com.uidai.governance.common.audit;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes immutable audit-trail entries. Audit writes participate in the caller's
 * transaction so that an action and its audit record commit atomically.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository repository;
    private final AuditorAware<String> auditorAware;

    public AuditLogService(AuditLogRepository repository, AuditorAware<String> auditorAware) {
        this.repository = repository;
        this.auditorAware = auditorAware;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(AuditAction action, String entityType, Long entityId, String details) {
        String actor = auditorAware.getCurrentAuditor().orElse("system");
        repository.save(new AuditLog(action, entityType, entityId, actor, Instant.now(), details));
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AuditLog> forEntity(
            String entityType, Long entityId, org.springframework.data.domain.Pageable pageable) {
        return repository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }
}
