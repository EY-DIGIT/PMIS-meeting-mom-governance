package com.uidai.governance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uidai.governance.external.notification.dto.EmailNotificationRequest;
import com.uidai.governance.external.notification.dto.EmailNotificationResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pins both directions of the POST /notification/email/send contract. */
class EmailPayloadShapeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void requestSerializesToTheNotificationApiContract() throws Exception {
        String json = objectMapper.writeValueAsString(
                EmailNotificationRequest.html(List.of("Saurabh.Pandey@in.ey.com"),
                        "Welcome to PMIS", "<h1>Hello</h1>"));

        assertEquals("{\"to\":[\"Saurabh.Pandey@in.ey.com\"],"
                + "\"subject\":\"Welcome to PMIS\","
                + "\"body\":\"<h1>Hello</h1>\","
                + "\"is_html\":true}", json);
    }

    @Test
    void responseDeserializesFromTheNotificationApiContract() throws Exception {
        EmailNotificationResponse response = objectMapper.readValue("""
                {
                  "success": true,
                  "message": "Email sent successfully",
                  "provider": "smtp",
                  "message_id": "smtp-6984c98f-7370-48a8-9530-92ea382eb607"
                }""", EmailNotificationResponse.class);

        assertTrue(response.success());
        assertEquals("Email sent successfully", response.message());
        assertEquals("smtp", response.provider());
        assertEquals("smtp-6984c98f-7370-48a8-9530-92ea382eb607", response.messageId());
    }
}
