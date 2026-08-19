package com.uidai.governance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uidai.governance.external.notification.dto.EmailNotificationRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pins the notification payload to the contract of POST /notification/email/send. */
class EmailPayloadShapeTest {

    @Test
    void serializesToTheNotificationApiContract() throws Exception {
        String json = new ObjectMapper().writeValueAsString(
                EmailNotificationRequest.html(List.of("Saurabh.Pandey@in.ey.com"),
                        "Welcome to PMIS", "<h1>Hello</h1>"));

        assertEquals("{\"to\":[\"Saurabh.Pandey@in.ey.com\"],"
                + "\"subject\":\"Welcome to PMIS\","
                + "\"body\":\"<h1>Hello</h1>\","
                + "\"is_html\":true}", json);
    }
}
