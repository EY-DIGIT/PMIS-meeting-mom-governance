-- Distinguish external guest attendees from internal (User-service) attendees.
ALTER TABLE meeting_participant
    ADD COLUMN is_external BOOLEAN NOT NULL DEFAULT FALSE;
