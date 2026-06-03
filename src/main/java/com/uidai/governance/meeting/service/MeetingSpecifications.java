package com.uidai.governance.meeting.service;

import com.uidai.governance.meeting.domain.Meeting;
import com.uidai.governance.meeting.domain.MeetingStatus;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable JPA specifications powering filter &amp; report queries
 * (MEET-FR-02.3).
 */
public final class MeetingSpecifications {

    private MeetingSpecifications() {
    }

    public static Specification<Meeting> project(String projectId) {
        return (root, query, cb) -> projectId == null ? null
                : cb.equal(root.get("projectId"), projectId);
    }

    public static Specification<Meeting> status(MeetingStatus status) {
        return (root, query, cb) -> status == null ? null
                : cb.equal(root.get("status"), status);
    }

    public static Specification<Meeting> from(LocalDate from) {
        return (root, query, cb) -> from == null ? null
                : cb.greaterThanOrEqualTo(root.get("meetingDate"), from);
    }

    public static Specification<Meeting> to(LocalDate to) {
        return (root, query, cb) -> to == null ? null
                : cb.lessThanOrEqualTo(root.get("meetingDate"), to);
    }
}
