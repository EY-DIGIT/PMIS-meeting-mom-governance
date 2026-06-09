-- Store the attachment metadata returned by the activity-create API (id, filename,
-- url, ...) on the meeting, so it can be returned by the meeting create/get/update APIs.
ALTER TABLE meeting
    ADD COLUMN IF NOT EXISTS attachments JSONB;
