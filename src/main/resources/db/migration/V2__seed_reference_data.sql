-- Seed the configurable meeting categories required by MEET-FR-02 / MEET-FR-02.4.
INSERT INTO meeting_type (code, display_name, description, requires_approval, active, created_by, created_at)
VALUES
    ('STEERING',   'Steering Committee', 'Strategic steering meetings',                 TRUE,  TRUE, 'system', now()),
    ('GOVERNANCE', 'Governance Review',  'Programme governance and oversight meetings', TRUE,  TRUE, 'system', now()),
    ('MIGRATION',  'Migration Review',   'Data / system migration meetings',            TRUE,  TRUE, 'system', now()),
    ('ADHOC',      'Ad-hoc',             'Unscheduled / ad-hoc meetings',               FALSE, TRUE, 'system', now())
ON CONFLICT (code) DO NOTHING;

-- Seed a default standardized MoM template (MEET-FR-05.1).
INSERT INTO mom_template (name, description, structure, active, created_by, created_at)
VALUES (
    'UIDAI Standard MoM',
    'Default standardized Minutes-of-Meeting layout for UIDAI governance meetings',
    '{"sections":["Attendees","Agenda","Discussion","Decisions","Action Items","Risks","Next Steps"]}',
    TRUE,
    'system',
    now()
)
ON CONFLICT (name) DO NOTHING;
