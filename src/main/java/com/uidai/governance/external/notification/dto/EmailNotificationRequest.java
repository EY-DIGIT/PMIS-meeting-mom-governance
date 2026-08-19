package com.uidai.governance.external.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Payload posted to the external notification API
 * ({@code POST /notification/email/send}).
 *
 * <p>One request carries every recipient of a single mail: all meeting attendees
 * are sent in {@code to}. The service requires all four fields; {@code is_html}
 * in particular is mandatory and the request is rejected with HTTP 422 without
 * it.</p>
 *
 * <pre>
 * {
 *   "to": ["someone@in.ey.com"],
 *   "subject": "Welcome to PMIS",
 *   "body": "&lt;h1&gt;Hello&lt;/h1&gt;",
 *   "is_html": true
 * }
 * </pre>
 *
 * @param to      recipient email addresses
 * @param subject mail subject line
 * @param body    mail body
 * @param isHtml  whether {@code body} is HTML (sent as {@code is_html})
 */
public record EmailNotificationRequest(
        List<String> to,
        String subject,
        String body,
        @JsonProperty("is_html") boolean isHtml
) {
    /** An HTML-bodied mail, which is what this module always sends. */
    public static EmailNotificationRequest html(List<String> to, String subject, String body) {
        return new EmailNotificationRequest(to, subject, body, true);
    }
}
