ALTER TABLE feedback_submissions ADD COLUMN display_order INT NOT NULL DEFAULT 0;
CREATE INDEX idx_feedback_display_order ON feedback_submissions(status, display_order);