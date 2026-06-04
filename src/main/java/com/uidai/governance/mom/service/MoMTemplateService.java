package com.uidai.governance.mom.service;

import com.uidai.governance.common.exception.BusinessValidationException;
import com.uidai.governance.common.exception.ResourceNotFoundException;
import com.uidai.governance.mom.domain.MoMTemplate;
import com.uidai.governance.mom.dto.MoMTemplateDto;
import com.uidai.governance.mom.repository.MoMTemplateRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Administration of standardized MoM templates (MEET-FR-05.1).
 */
@Service
public class MoMTemplateService {

    private final MoMTemplateRepository repository;

    public MoMTemplateService(MoMTemplateRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<MoMTemplateDto> listActive() {
        return repository.findByActiveTrue().stream().map(MoMTemplateDto::from).toList();
    }

    @Transactional(readOnly = true)
    public MoMTemplateDto get(Long id) {
        return MoMTemplateDto.from(requireById(id));
    }

    @Transactional
    public MoMTemplateDto create(MoMTemplateDto dto) {
        if (repository.existsByName(dto.name())) {
            throw new BusinessValidationException("Template name already exists: " + dto.name());
        }
        MoMTemplate template = new MoMTemplate(dto.name(), dto.description(), dto.structure());
        template.setActive(dto.active());
        return MoMTemplateDto.from(repository.save(template));
    }

    @Transactional
    public MoMTemplateDto update(Long id, MoMTemplateDto dto) {
        MoMTemplate template = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MoMTemplate", id));
        template.setName(dto.name());
        template.setDescription(dto.description());
        template.setStructure(dto.structure());
        template.setActive(dto.active());
        return MoMTemplateDto.from(repository.save(template));
    }

    @Transactional(readOnly = true)
    public MoMTemplate requireById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MoMTemplate", id));
    }
}
