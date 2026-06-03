-- Link MoM action items to external project activities (GET/PATCH /activities/{id}).
ALTER TABLE action_item
    ADD COLUMN IF NOT EXISTS linked_activity_id VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_action_activity ON action_item (linked_activity_id);
