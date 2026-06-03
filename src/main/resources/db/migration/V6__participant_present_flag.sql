-- Records which attendees were actually present (set when updating the meeting).
ALTER TABLE meeting_participant
    ADD COLUMN is_present BOOLEAN NOT NULL DEFAULT FALSE;
