package com.uidai.governance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: verifies the Spring context (entities, repositories, services,
 * external clients) wires up against the in-memory H2 database.
 */
@SpringBootTest
class MeetingsMomApplicationTests {

    @Test
    void contextLoads() {
    }
}
