package com.uidai.governance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * UIDAI Meetings &amp; Minutes of Meeting (MoM) Governance Module.
 *
 * <p>Provides a structured, auditable system for capturing meeting outcomes,
 * assigning and tracking action items, and ensuring accountability across
 * governance, steering, and migration meetings involving UIDAI, PMC and MSP
 * stakeholders.</p>
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@ConfigurationPropertiesScan
public class MeetingsMomApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetingsMomApplication.class, args);
    }
}
