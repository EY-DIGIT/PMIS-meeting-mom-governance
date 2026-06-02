package com.uidai.governance.meeting.repository;

import com.uidai.governance.meeting.domain.MeetingType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingTypeRepository extends JpaRepository<MeetingType, Long> {

    Optional<MeetingType> findByCode(String code);

    boolean existsByCode(String code);

    List<MeetingType> findByActiveTrue();
}
