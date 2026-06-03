-- Link a meeting to the project activity created for it in the external
-- (Python) project service when the meeting is recorded (MEET-FR-01.2).
-- The activity's project and milestone ids are UUIDs from that service and are
-- stored as text, distinct from the module's numeric project_id.
ALTER TABLE meeting
    ADD COLUMN activity_id            VARCHAR(64),
    ADD COLUMN activity_project_id    VARCHAR(64),
    ADD COLUMN activity_milestone_id  VARCHAR(64),
    ADD COLUMN activity_name          VARCHAR(250),
    ADD COLUMN activity_description   VARCHAR(5000);

CREATE INDEX idx_meeting_activity ON meeting (activity_id);
