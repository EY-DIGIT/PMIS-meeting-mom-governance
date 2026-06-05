-- Cache the project's code and name (resolved from the external project service)
-- on the meeting so they can be returned without re-fetching on every read.
ALTER TABLE meeting
    ADD COLUMN IF NOT EXISTS project_code VARCHAR(100),
    ADD COLUMN IF NOT EXISTS project_name VARCHAR(250);
