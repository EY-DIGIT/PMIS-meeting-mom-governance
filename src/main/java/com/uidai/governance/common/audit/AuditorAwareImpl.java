package com.uidai.governance.common.audit;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Resolves the acting user for JPA auditing.
 *
 * <p>Until the central identity integration is finalised, the caller identity is
 * taken from the {@code X-User-Id} request header (populated by the API gateway /
 * the external User service). Falls back to {@code system} for background work.</p>
 */
@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {

    public static final String USER_HEADER = "X-User-Id";
    private static final String SYSTEM = "system";

    @Override
    public Optional<String> getCurrentAuditor() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            String userId = attrs.getRequest().getHeader(USER_HEADER);
            if (userId != null && !userId.isBlank()) {
                return Optional.of(userId);
            }
        }
        return Optional.of(SYSTEM);
    }
}
