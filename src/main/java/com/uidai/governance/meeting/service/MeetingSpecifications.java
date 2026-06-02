package com.uidai.governance.meeting.service;

import com.uidai.governance.meeting.domain.Meeting;
import com.uidai.governance.meeting.domain.MeetingStatus;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable JPA specifications powering filter &amp; report queries
 * (MEET-FR-02.3).
 */
public final class MeetingSpecifications {

    private MeetingSpecifications() {
    }

    public static Specification<Meeting> typeCode(String code) {
        return (root, query, cb) -> code == null ? null
                : cb.equal(root.get("meetingType").get("code"), code);
    }

    public static Specification<Meeting> project(Long projectId) {
        return (root, query, cb) -> projectId == null ? null
                : cb.equal(root.get("projectId"), projectId);
    }

    public static Specification<Meeting> serviceProvider(Long serviceProviderId) {
        return (root, query, cb) -> serviceProviderId == null ? null
                : cb.equal(root.get("serviceProviderId"), serviceProviderId);
    }

    public static Specification<Meeting> status(MeetingStatus status) {
        return (root, query, cb) -> status == null ? null
                : cb.equal(root.get("status"), status);
    }

    public static Specification<Meeting> from(Instant from) {
        return (root, query, cb) -> from == null ? null
                : cb.greaterThanOrEqualTo(root.get("meetingDate"), from);
    }

    public static Specification<Meeting> to(Instant to) {
        return (root, query, cb) -> to == null ? null
                : cb.lessThanOrEqualTo(root.get("meetingDate"), to);
    }
}
