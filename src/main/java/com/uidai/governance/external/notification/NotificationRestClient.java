package com.uidai.governance.external.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uidai.governance.common.logging.JsonLogFormatter;
import com.uidai.governance.config.NotificationProperties;
import com.uidai.governance.config.RestClientConfig;
import com.uidai.governance.external.notification.dto.EmailNotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * REST implementation of {@link NotificationClient} backed by the external
 * notification API ({@code POST} to {@code app.notification.url}, default
 * {@code http://10.1.131.199:8012/notification/email/send}).
 *
 * <p>Delivery is <em>best-effort</em>: any failure (transport error, non-2xx
 * response) is logged and swallowed so that notifications can never roll back or
 * fail the business operation that triggered them. When
 * {@code app.notification.enabled=false} the call short-circuits.</p>
 *
 * <p>The payload is serialized here and sent as a byte array with an explicit
 * {@code Content-Length}. This matters: Spring's request factories stream the
 * body and fall back to {@code Transfer-Encoding: chunked} when the length is
 * unknown, and the notification service rejects chunked requests outright with
 * {@code 400 "Invalid HTTP request received."} (and, before that, reads an empty
 * body and reports the payload as missing).</p>
 */
@Component
public class NotificationRestClient implements NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationRestClient.class);

    private final RestClient restClient;
    private final NotificationProperties properties;
    private final JsonLogFormatter jsonLog;
    private final ObjectMapper objectMapper;

    public NotificationRestClient(@Qualifier(RestClientConfig.NOTIFICATION_CLIENT) RestClient restClient,
                                  NotificationProperties properties,
                                  JsonLogFormatter jsonLog,
                                  ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.properties = properties;
        this.jsonLog = jsonLog;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean sendEmail(EmailNotificationRequest request) {
        if (!properties.enabled()) {
            log.debug("Notification API disabled - skipping email");
            return false;
        }
        if (request.to() == null || request.to().isEmpty()) {
            log.debug("No recipients resolved - skipping notification email");
            return false;
        }
        log.info("Calling Notification API: POST {} request={}",
                properties.url(), jsonLog.toJson(request));
        try {
            // Serialize up-front so the exact byte count can be declared: see the
            // class javadoc on why a chunked body is not acceptable here.
            byte[] payload = objectMapper.writeValueAsBytes(request);
            
            restClient.post()
                    .uri(properties.url())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Notification email accepted for {} recipient(s)", request.to().size());
            return true;
        } catch (Exception ex) {
            // Best-effort: never propagate - the caller's operation has already succeeded.
            log.error("Notification API call failed: POST {} request={}",
                    properties.url(), jsonLog.toJson(request), ex);
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        return properties.enabled();
    }
}