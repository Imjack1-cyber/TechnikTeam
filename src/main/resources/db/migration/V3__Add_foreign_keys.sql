-- Flyway migration V3: Add all foreign key constraints.

START TRANSACTION;

ALTER TABLE `course_attendance`
  ADD CONSTRAINT `course_attendance_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `course_attendance_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE;

ALTER TABLE `events`
  ADD CONSTRAINT `fk_event_leader` FOREIGN KEY (`leader_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

ALTER TABLE `event_assignments`
  ADD CONSTRAINT `event_assignments_ibfk_1` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `event_assignments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

ALTER TABLE `event_attendance`
  ADD CONSTRAINT `event_attendance_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `event_attendance_ibfk_2` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`) ON DELETE CASCADE;

ALTER TABLE `event_chat_messages`
  ADD CONSTRAINT `event_chat_messages_ibfk_1` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `event_chat_messages_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

ALTER TABLE `event_custom_fields`
  ADD CONSTRAINT `event_custom_fields_ibfk_1` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`) ON DELETE CASCADE;

ALTER TABLE `event_custom_field_responses`
  ADD CONSTRAINT `event_custom_field_responses_ibfk_1` FOREIGN KEY (`field_id`) REFERENCES `event_custom_fields` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `event_custom_field_responses_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

ALTER TABLE `event_skill_requirements`
  ADD CONSTRAINT `event_skill_requirements_ibfk_1` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `event_skill_requirements_ibfk_2` FOREIGN KEY (`required_course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE;

ALTER TABLE `event_storage_reservations`
  ADD CONSTRAINT `event_storage_reservations_ibfk_1` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `event_storage_reservations_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `storage_items` (`id`) ON DELETE CASCADE;

ALTER TABLE `event_tasks`
  ADD CONSTRAINT `event_tasks_ibfk_1` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`) ON DELETE CASCADE;

ALTER TABLE `event_task_assignments`
  ADD CONSTRAINT `event_task_assignments_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `event_tasks` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `event_task_assignments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

ALTER TABLE `event_task_kits`
  ADD CONSTRAINT `event_task_kits_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `event_tasks` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `event_task_kits_ibfk_2` FOREIGN KEY (`kit_id`) REFERENCES `inventory_kits` (`id`) ON DELETE CASCADE;

ALTER TABLE `event_task_storage_items`
  ADD CONSTRAINT `event_task_storage_items_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `event_tasks` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `event_task_storage_items_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `storage_items` (`id`) ON DELETE CASCADE;

ALTER TABLE `feedback_forms`
  ADD CONSTRAINT `feedback_forms_ibfk_1` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`) ON DELETE CASCADE;

ALTER TABLE `feedback_responses`
  ADD CONSTRAINT `feedback_responses_ibfk_1` FOREIGN KEY (`form_id`) REFERENCES `feedback_forms` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `feedback_responses_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

ALTER TABLE `feedback_submissions`
  ADD CONSTRAINT `feedback_submissions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

ALTER TABLE `files`
  ADD CONSTRAINT `files_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `file_categories` (`id`) ON DELETE SET NULL;

ALTER TABLE `inventory_kit_items`
  ADD CONSTRAINT `inventory_kit_items_ibfk_1` FOREIGN KEY (`kit_id`) REFERENCES `inventory_kits` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `inventory_kit_items_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `storage_items` (`id`) ON DELETE CASCADE;

ALTER TABLE `maintenance_log`
  ADD CONSTRAINT `maintenance_log_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `storage_items` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `maintenance_log_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `meetings`
  ADD CONSTRAINT `fk_meeting_leader` FOREIGN KEY (`leader_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `meetings_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE;

ALTER TABLE `meeting_attendance`
  ADD CONSTRAINT `meeting_attendance_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `meeting_attendance_ibfk_2` FOREIGN KEY (`meeting_id`) REFERENCES `meetings` (`id`) ON DELETE CASCADE;

ALTER TABLE `profile_change_requests`
  ADD CONSTRAINT `profile_change_requests_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `profile_change_requests_ibfk_2` FOREIGN KEY (`reviewed_by_admin_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

ALTER TABLE `storage_items`
  ADD CONSTRAINT `fk_assigned_event` FOREIGN KEY (`assigned_event_id`) REFERENCES `events` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_holder_user` FOREIGN KEY (`current_holder_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

ALTER TABLE `storage_log`
  ADD CONSTRAINT `fk_log_event` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `storage_log_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `storage_items` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `storage_log_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

ALTER TABLE `todo_tasks`
    ADD CONSTRAINT `fk_todo_category` FOREIGN KEY (`category_id`) REFERENCES `todo_categories`(`id`) ON DELETE CASCADE;

ALTER TABLE `users`
  ADD CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE SET NULL;

ALTER TABLE `user_achievements`
  ADD CONSTRAINT `user_achievements_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `user_achievements_ibfk_2` FOREIGN KEY (`achievement_id`) REFERENCES `achievements` (`id`) ON DELETE CASCADE;

ALTER TABLE `user_passkeys`
  ADD CONSTRAINT `user_passkeys_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

ALTER TABLE `user_permissions`
  ADD CONSTRAINT `user_permissions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `user_permissions_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE;

ALTER TABLE `user_qualifications`
  ADD CONSTRAINT `user_qualifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `user_qualifications_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE;

COMMIT;