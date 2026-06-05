package com.uidai.governance.mom.repository;

import com.uidai.governance.mom.domain.MinutesOfMeeting;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface MinutesOfMeetingRepository extends JpaRepository<MinutesOfMeeting, Long> {

    Optional<MinutesOfMeeting> findByMeetingId(UUID meetingId);

    boolean existsByMeetingId(UUID meetingId);

    /**
     * Free-text search across MoM title and content (MEET-FR-05.2). Case
     * insensitive. A null/blank term returns all records.
     */
    @Query("""
            select m from MinutesOfMeeting m
            where :term is null
               or lower(m.title) like lower(concat('%', :term, '%'))
               or lower(m.content) like lower(concat('%', :term, '%'))
            """)
    Page<MinutesOfMeeting> search(@Param("term") String term, Pageable pageable);
}
