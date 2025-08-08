-- V27__Add_Meeting_Waitlist.sql
-- Add parent_meeting_id to meetings to reference original meeting when creating repeats
ALTER TABLE meetings
  ADD COLUMN parent_meeting_id INT NULL AFTER course_id,
  ADD CONSTRAINT fk_meetings_parent FOREIGN KEY (parent_meeting_id) REFERENCES meetings (id) ON DELETE SET NULL;

-- Create meeting_waitlist table to store waitlist entries (separate from attendance)
CREATE TABLE meeting_waitlist (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  meeting_id INT NOT NULL,
  user_id INT NOT NULL,
  requested_by INT NULL, -- user who made the request (usually same as user_id, but left for auditing)
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  promoted_by INT NULL,
  promoted_at TIMESTAMP NULL,
  CONSTRAINT fk_waitlist_meeting FOREIGN KEY (meeting_id) REFERENCES meetings (id) ON DELETE CASCADE,
  CONSTRAINT fk_waitlist_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT uniq_waitlist_user_meeting UNIQUE (meeting_id, user_id),
  INDEX idx_waitlist_meeting_created (meeting_id, created_at)
);
