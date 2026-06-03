package com.uidai.governance.mom.domain;

import com.uidai.governance.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Standardized MoM template (MEET-FR-05.1). Holds the section structure that a
 * MoM should follow; stored as JSON/markdown so templates can evolve without a
 * schema change.
 */
@Entity
@Table(name = "mom_template")
public class MoMTemplate extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    /** Section layout / boilerplate (JSON or markdown). */
    @Column(name = "structure", nullable = false, length = 8000)
    private String structure;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected MoMTemplate() {
    }

    public MoMTemplate(String name, String description, String structure) {
        this.name = name;
        this.description = description;
        this.structure = structure;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
