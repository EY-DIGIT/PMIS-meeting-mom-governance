package com.uidai.governance.meeting.repository;

import com.uidai.governance.meeting.domain.Meeting;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for meetings. Extends {@link JpaSpecificationExecutor} to support
 * the dynamic filtering/reporting required by MEET-FR-02.3.
 */
public interface MeetingRepository extends JpaRepository<Meeting, UUID>,
        JpaSpecificationExecutor<Meeting> {

    /** Next value of the meeting-code sequence, used to build codes like {@code M<n>}. */
    @Query(value = "SELECT nextval('meeting_code_seq')", nativeQuery = true)
    long nextMeetingCodeSeq();
}
