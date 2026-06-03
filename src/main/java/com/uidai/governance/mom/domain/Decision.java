package com.uidai.governance.mom.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A decision recorded within a MoM (part of MEET-FR-04.2 structuring).
 */
@Entity
@Table(name = "mom_decision")
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mom_id", nullable = false)
    private MinutesOfMeeting mom;

    @Column(name = "description", nullable = false, length = 4000)
    private String description;

    /** Optional owner (external user id) accountable for the decision. */
    @Column(name = "owner_user_id", length = 100)
    private String ownerUserId;

    protected Decision() {
    }

    public Decision(String description, String ownerUserId) {
        this.description = description;
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

    public String getOwnerUserId() {
        return ownerUserId;
    }
}
