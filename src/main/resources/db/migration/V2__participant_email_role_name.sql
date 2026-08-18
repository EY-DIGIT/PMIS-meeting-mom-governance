-- Attendee contact details on meeting_participant.
--
-- email is the address the meeting invite is sent to. It is mandatory on the
-- API for internal attendees; for external attendees the address is already held
-- in user_id and is copied here too, so every participant row carries the
-- address the invite went to. role_name is the attendee's role name, kept
-- separate from participant_role (their role at the meeting itself).
--
-- Both are nullable at the database level so pre-existing rows (recorded before
-- these fields existed) remain valid.
ALTER TABLE meeting_participant
    ADD COLUMN IF NOT EXISTS email VARCHAR(150);

ALTER TABLE meeting_participant
    ADD COLUMN IF NOT EXISTS role_name VARCHAR(150);

-- Backfill external attendees, whose email address is stored as the user_id.
UPDATE meeting_participant
SET email = user_id
WHERE is_external = TRUE
  AND email IS NULL;
