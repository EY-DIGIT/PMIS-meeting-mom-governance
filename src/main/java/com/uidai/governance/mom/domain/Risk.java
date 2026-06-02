package com.uidai.governance.mom.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A risk recorded within a MoM (part of MEET-FR-04.2 structuring).
 */
@Entity
@Table(name = "mom_risk")
public class Risk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mom_id", nullable = false)
    private MinutesOfMeeting mom;

    @Column(name = "description", nullable = false, length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private RiskSeverity severity = RiskSeverity.MEDIUM;

    @Column(name = "mitigation", length = 4000)
    private String mitigation;

    @Column(name = "owner_user_id", length = 100)
    private String ownerUserId;

    protected Risk() {
    }

    public Risk(String description, RiskSeverity severity, String mitigation, String ownerUserId) {
        this.description = description;
        this.severity = severity;
        this.mitigation = mitigation;
        this.ownerUserId = ownerUserId;
    }

    public Long getId() {
        return id;
    }

    public MinutesOfMeeting getMom() {
        return mom;
    }

    public void setMom(MinutesOfMeeting mom) {
        this.mom = mom;
    }

    public String getDescription() {
        return description;
    }

    public RiskSeverity getSeverity() {
        return severity;
    }

    public String getMitigation() {
        return mitigation;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }
}
