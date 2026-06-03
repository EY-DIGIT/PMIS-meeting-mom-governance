-- Link MoM action items to external project activities (GET/PATCH /activities/{id}).
ALTER TABLE action_item
    ADD COLUMN linked_activity_id VARCHAR(100);

CREATE INDEX idx_action_activity ON action_item (linked_activity_id);
