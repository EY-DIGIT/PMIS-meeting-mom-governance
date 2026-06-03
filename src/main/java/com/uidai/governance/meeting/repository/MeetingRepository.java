package com.uidai.governance.meeting.repository;

import com.uidai.governance.meeting.domain.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository for meetings. Extends {@link JpaSpecificationExecutor} to support
 * the dynamic filtering/reporting required by MEET-FR-02.3.
 */
public interface MeetingRepository extends JpaRepository<Meeting, Long>,
        JpaSpecificationExecutor<Meeting> {
}
