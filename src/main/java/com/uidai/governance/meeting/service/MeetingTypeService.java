package com.uidai.governance.meeting.service;

import com.uidai.governance.common.exception.BusinessValidationException;
import com.uidai.governance.common.exception.ResourceNotFoundException;
import com.uidai.governance.meeting.domain.MeetingType;
import com.uidai.governance.meeting.dto.MeetingTypeDto;
import com.uidai.governance.meeting.repository.MeetingTypeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Administration of configurable meeting types (MEET-FR-02.4).
 */
@Service
public class MeetingTypeService {

    private final MeetingTypeRepository repository;

    public MeetingTypeService(MeetingTypeRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<MeetingTypeDto> listActive() {
        return repository.findByActiveTrue().stream().map(MeetingTypeDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MeetingTypeDto> listAll() {
        return repository.findAll().stream().map(MeetingTypeDto::from).toList();
    }

    @Transactional
    public MeetingTypeDto create(MeetingTypeDto dto) {
        if (repository.existsByCode(dto.code())) {
            throw new BusinessValidationException("Meeting type code already exists: " + dto.code());
        }
        MeetingType type = new MeetingType(dto.code(), dto.displayName(), dto.description(),
                dto.requiresApproval());
        type.setActive(dto.active());
        return MeetingTypeDto.from(repository.save(type));
    }

    @Transactional
    public MeetingTypeDto update(Long id, MeetingTypeDto dto) {
        MeetingType type = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MeetingType", id));
        type.setDisplayName(dto.displayName());
        type.setDescription(dto.description());
        type.setRequiresApproval(dto.requiresApproval());
        type.setActive(dto.active());
        return MeetingTypeDto.from(repository.save(type));
    }

    /** Resolves a type by code or fails if missing/inactive. */
    @Transactional(readOnly = true)
    public MeetingType requireActiveByCode(String code) {
        MeetingType type = repository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("MeetingType", code));
        if (!type.isActive()) {
            throw new BusinessValidationException("Meeting type is not active: " + code);
        }
        return type;
    }
}
