-- Distinguish external guest attendees from internal (User-service) attendees.
ALTER TABLE meeting_participant
    ADD COLUMN IF NOT EXISTS is_external BOOLEAN NOT NULL DEFAULT FALSE;
