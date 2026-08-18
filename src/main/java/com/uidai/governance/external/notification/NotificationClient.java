package com.uidai.governance.external.notification;

import com.uidai.governance.external.notification.dto.EmailNotificationRequest;

/**
 * Abstraction over the external notification (email) API configured at
 * {@code app.notification.url}.
 */
public interface NotificationClient {

    /**
     * Sends one email to all recipients in the request. Implementations never
     * throw: delivery is best-effort and a failure must not fail the business
     * operation that triggered the mail.
     *
     * @return true when the notification API accepted the request
     */
    boolean sendEmail(EmailNotificationRequest request);

    /** Whether the notification integration is enabled ({@code app.notification.enabled}). */
    boolean isEnabled();
}
