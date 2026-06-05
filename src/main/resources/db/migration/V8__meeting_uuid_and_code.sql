-- Switch the meeting primary key to UUID and add a human-readable meeting code
-- (MEET.1, MEET.2, ...). A bigint -> uuid conversion cannot preserve existing
-- rows / FK links, so the (pre-release, test-only) meeting data is cleared first.
TRUNCATE TABLE meeting, meeting_participant, minutes_of_meeting,
               mom_decision, mom_risk, action_item, action_item_comment,
               timeline_extension_request RESTART IDENTITY CASCADE;

-- Audit entity_id must hold UUIDs (meeting) as well as bigints (MoM, action item).
ALTER TABLE audit_log ALTER COLUMN entity_id SET DATA TYPE VARCHAR(64) USING entity_id::text;

-- Drop the FKs that reference meeting.id before changing the PK type.
ALTER TABLE meeting_participant DROP CONSTRAINT IF EXISTS meeting_participant_meeting_id_fkey;
ALTER TABLE minutes_of_meeting  DROP CONSTRAINT IF EXISTS minutes_of_meeting_meeting_id_fkey;

-- meeting.id: bigint identity -> uuid (default to a random UUID).
ALTER TABLE meeting ALTER COLUMN id DROP IDENTITY IF EXISTS;
ALTER TABLE meeting ALTER COLUMN id SET DATA TYPE uuid USING gen_random_uuid();
ALTER TABLE meeting ALTER COLUMN id SET DEFAULT gen_random_uuid();

-- Referencing FK columns -> uuid (tables are empty after the truncate above).
ALTER TABLE meeting_participant ALTER COLUMN meeting_id SET DATA TYPE uuid USING NULL;
ALTER TABLE minutes_of_meeting  ALTER COLUMN meeting_id SET DATA TYPE uuid USING NULL;

-- Re-create the FKs.
ALTER TABLE meeting_participant
    ADD CONSTRAINT meeting_participant_meeting_id_fkey FOREIGN KEY (meeting_id) REFERENCES meeting (id);
ALTER TABLE minutes_of_meeting
    ADD CONSTRAINT minutes_of_meeting_meeting_id_fkey FOREIGN KEY (meeting_id) REFERENCES meeting (id);

-- Human-readable meeting code, assigned from a sequence as MEET.<n>.
CREATE SEQUENCE IF NOT EXISTS meeting_code_seq;
ALTER TABLE meeting ADD COLUMN IF NOT EXISTS meeting_code VARCHAR(30);
