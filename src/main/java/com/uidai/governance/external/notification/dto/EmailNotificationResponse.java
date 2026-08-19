package com.uidai.governance.external.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Response returned by the external notification API
 * ({@code POST /notification/email/send}):
 *
 * <pre>
 * {
 *   "success": true,
 *   "message": "Email sent successfully",
 *   "provider": "smtp",
 *   "message_id": "smtp-6984c98f-7370-48a8-9530-92ea382eb607"
 * }
 * </pre>
 *
 * <p>{@code success} is authoritative: the service can answer HTTP 200 with
 * {@code success=false} when the provider rejected the mail, so the flag is
 * checked rather than the status code alone. Field names are mapped from the
 * service's {@code snake_case} JSON and unknown properties are ignored so the
 * service can evolve without breaking this client.</p>
 *
 * @param success   whether the mail was accepted by the provider
 * @param message   human-readable outcome, e.g. "Email sent successfully"
 * @param provider  delivery provider that handled the mail, e.g. "smtp"
 * @param messageId provider-side id, useful for tracing a delivery
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record EmailNotificationResponse(
        boolean success,
        String message,
        String provider,
        String messageId
) {
}
