package com.uidai.governance.external.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Payload posted to the external notification API
 * ({@code POST /notification/email/send}).
 *
 * <p>One request carries every recipient of a single mail: all meeting attendees
 * are sent in {@code to}.</p>
 *
 * <p><strong>TODO (awaiting API signature):</strong> the field names below are
 * the assumed contract of the notification service. If the service expects
 * different keys (e.g. {@code recipients} / {@code message} / {@code html_body}),
 * rename them here — this record is the only place they are declared.</p>
 *
 * @param to      recipient email addresses
 * @param subject mail subject line
 * @param body    mail body (HTML)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmailNotificationRequest(
        List<String> to,
        String subject,
        String body
) {
}
