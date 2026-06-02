package com.uidai.governance.mom.domain;

import com.uidai.governance.common.audit.AuditableEntity;
import com.uidai.governance.meeting.domain.Meeting;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;

/**
 * Minutes of a Meeting (MEET-FR-04, MEET-FR-05).
 *
 * <p>Carries free-form content (MEET-FR-04.1) structured into decisions, action
 * items and risks (MEET-FR-04.2), follows a standardized template
 * (MEET-FR-05.1), and is searchable / linkable to project artefacts
 * (MEET-FR-05.2). A FINALIZED MoM is part of the official UIDAI record
 * (MEET-FR-05.3).</p>
 */
@Entity
@Table(name = "minutes_of_meeting",
        uniqueConstraints = @UniqueConstraint(name = "uk_mom_meeting", columnNames = "meeting_id"))
public class MinutesOfMeeting extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private MoMTemplate template;

    @Column(name = "title", nullable = false, length = 250)
    private String title;

    /** Free-form MoM body (MEET-FR-04.1). */
    @Column(name = "content", length = 20000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MoMStatus status = MoMStatus.DRAFT;

    @OneToMany(mappedBy = "mom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Decision> decisions = new ArrayList<>();

    @OneToMany(mappedBy = "mom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActionItem> actionItems = new ArrayList<>();

    @OneToMany(mappedBy = "mom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Risk> risks = new ArrayList<>();

    protected MinutesOfMeeting() {
    }

    public MinutesOfMeeting(Meeting meeting, MoMTemplate template, String title, String content) {
        this.meeting = meeting;
        this.template = template;
        this.title = title;
        this.content = content;
        this.status = MoMStatus.DRAFT;
    }

    public void addDecision(Decision decision) {
        decision.setMom(this);
        this.decisions.add(decision);
    }

    public void addActionItem(ActionItem item) {
        item.setMom(this);
        this.actionItems.add(item);
    }

    public void addRisk(Risk risk) {
        risk.setMom(this);
        this.risks.add(risk);
    }

    public Long getId() {
        return id;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public MoMTemplate getTemplate() {
        return template;
    }

    public void setTemplate(MoMTemplate template) {
        this.template = template;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MoMStatus getStatus() {
        return status;
    }

    public void setStatus(MoMStatus status) {
        this.status = status;
    }

    public List<Decision> getDecisions() {
        return decisions;
    }

    public List<ActionItem> getActionItems() {
        return actionItems;
    }

    public List<Risk> getRisks() {
        return risks;
    }
}
