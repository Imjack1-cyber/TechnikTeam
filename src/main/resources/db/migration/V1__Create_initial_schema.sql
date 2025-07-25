-- This script contains the full DDL to create the database schema from scratch.
-- It has been reordered to ensure all tables are created before constraints are applied.
-- ALL `INSERT` statements have been removed as Flyway manages schema, not data.

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- ===================================================================
-- PART 1: CREATE ALL TABLES
-- ===================================================================

CREATE TABLE `achievements` (
  `id` int(11) NOT NULL,
  `achievement_key` varchar(50) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `icon_class` varchar(50) DEFAULT 'fa-award'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `admin_logs` (
  `id` int(11) NOT NULL,
  `admin_username` varchar(50) DEFAULT NULL,
  `action_type` varchar(255) DEFAULT NULL,
  `details` text DEFAULT NULL,
  `action_timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `attachments` (
  `id` int(11) NOT NULL,
  `parent_type` enum('EVENT','MEETING') NOT NULL,
  `parent_id` int(11) NOT NULL,
  `filename` varchar(255) NOT NULL,
  `filepath` varchar(255) NOT NULL,
  `required_role` enum('NUTZER','ADMIN') NOT NULL DEFAULT 'NUTZER',
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `courses` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `abbreviation` varchar(20) DEFAULT NULL,
  `description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `course_attendance` (
  `user_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `signup_status` enum('ANGEMELDET','ABGEMELDET') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `events` (
  `id` int(11) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `event_datetime` datetime NOT NULL,
  `end_datetime` datetime DEFAULT NULL,
  `description` text DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `status` enum('GEPLANT','KOMPLETT','LAUFEND','ABGESCHLOSSEN','ABGESAGT') NOT NULL DEFAULT 'GEPLANT',
  `leader_user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_assignments` (
  `assignment_id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_attendance` (
  `user_id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `signup_status` enum('ANGEMELDET','ABGEMELDET') NOT NULL,
  `commitment_status` enum('BESTÄTIGT','OFFEN','ZUGESAGT') NOT NULL DEFAULT 'OFFEN'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_chat_messages` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  `message_text` text DEFAULT NULL,
  `edited` tinyint(1) NOT NULL DEFAULT 0,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `deleted_by_user_id` int(11) DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `deleted_by_username` varchar(255) DEFAULT NULL,
  `sent_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_custom_fields` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `field_name` varchar(255) DEFAULT NULL,
  `field_type` enum('TEXT','BOOLEAN','DROPDOWN','CHECKBOX_GROUP') NOT NULL,
  `is_required` tinyint(1) NOT NULL DEFAULT 0,
  `field_options` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_custom_field_responses` (
  `id` int(11) NOT NULL,
  `field_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `response_value` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_skill_requirements` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `required_course_id` int(11) NOT NULL,
  `required_persons` int(11) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_storage_reservations` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `reserved_quantity` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_tasks` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `description` text NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'OFFEN',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `display_order` int(11) NOT NULL DEFAULT 0,
  `required_persons` int(11) NOT NULL DEFAULT 0,
  `details` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_task_assignments` (
  `task_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_task_kits` (
  `task_id` int(11) NOT NULL,
  `kit_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_task_storage_items` (
  `task_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `feedback_forms` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `feedback_responses` (
  `id` int(11) NOT NULL,
  `form_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `rating` int(11) NOT NULL COMMENT 'e.g., 1 to 5 stars',
  `comments` text DEFAULT NULL,
  `submitted_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `feedback_submissions` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `subject` varchar(255) NOT NULL,
  `display_title` varchar(255) DEFAULT NULL,
  `content` text NOT NULL,
  `submitted_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` enum('NEW','VIEWED','PLANNED','REJECTED','COMPLETED') NOT NULL DEFAULT 'NEW',
  `display_order` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `files` (
  `id` int(11) NOT NULL,
  `filename` varchar(255) DEFAULT NULL,
  `filepath` varchar(512) DEFAULT NULL,
  `category_id` int(11) DEFAULT NULL,
  `required_role` varchar(20) DEFAULT NULL,
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `file_categories` (
  `id` int(11) NOT NULL,
  `name` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `inventory_kits` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `inventory_kit_items` (
  `kit_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `maintenance_log` (
  `id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `log_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `action` varchar(255) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `cost` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `meetings` (
  `id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `meeting_datetime` datetime NOT NULL,
  `end_datetime` datetime DEFAULT NULL,
  `leader_user_id` int(11) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `meeting_attendance` (
  `user_id` int(11) NOT NULL,
  `meeting_id` int(11) NOT NULL,
  `attended` tinyint(1) DEFAULT 0,
  `remarks` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `permissions` (
  `id` int(11) NOT NULL,
  `permission_key` varchar(100) NOT NULL COMMENT 'e.g., USER_CREATE, EVENT_DELETE',
  `description` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `profile_change_requests` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `requested_changes` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`requested_changes`)),
  `status` enum('PENDING','APPROVED','DENIED') NOT NULL DEFAULT 'PENDING',
  `requested_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `reviewed_by_admin_id` int(11) DEFAULT NULL,
  `reviewed_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `roles` (
  `id` int(11) NOT NULL,
  `role_name` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `shared_documents` (
  `id` int(11) NOT NULL,
  `document_name` varchar(100) NOT NULL,
  `content` text DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `storage_items` (
  `id` int(11) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `location` varchar(50) DEFAULT NULL,
  `cabinet` varchar(50) DEFAULT NULL,
  `shelf` varchar(50) DEFAULT NULL,
  `compartment` varchar(50) DEFAULT NULL,
  `quantity` int(11) NOT NULL DEFAULT 1,
  `max_quantity` int(11) NOT NULL DEFAULT 0,
  `defective_quantity` int(11) NOT NULL DEFAULT 0,
  `defect_reason` text DEFAULT NULL,
  `weight_kg` decimal(10,2) DEFAULT NULL,
  `price_eur` decimal(10,2) DEFAULT NULL,
  `image_path` varchar(512) DEFAULT NULL,
  `status` enum('IN_STORAGE','CHECKED_OUT','ASSIGNED_TO_EVENT','MAINTENANCE') NOT NULL DEFAULT 'IN_STORAGE',
  `current_holder_user_id` int(11) DEFAULT NULL,
  `assigned_event_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `storage_log` (
  `id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `quantity_change` int(11) NOT NULL,
  `notes` text DEFAULT NULL,
  `event_id` int(11) DEFAULT NULL,
  `transaction_timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `role_id` int(11) DEFAULT NULL,
  `class_year` int(11) DEFAULT NULL,
  `class_name` varchar(10) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `chat_color` varchar(7) DEFAULT '#E9ECEF',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `theme` varchar(10) DEFAULT 'light'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_achievements` (
  `user_id` int(11) NOT NULL,
  `achievement_id` int(11) NOT NULL,
  `earned_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_passkeys` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `credential_id` text NOT NULL,
  `public_key` text NOT NULL,
  `signature_count` bigint(20) NOT NULL,
  `user_handle` text NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_permissions` (
  `user_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_qualifications` (
  `user_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `completion_date` date DEFAULT NULL,
  `status` enum('BESUCHT','ABSOLVIERT') NOT NULL DEFAULT 'BESUCHT',
  `remarks` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- PART 2: ADD ALL PRIMARY KEYS, UNIQUE CONSTRAINTS, AND INDEXES
-- ===================================================================

ALTER TABLE `achievements` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `achievement_key` (`achievement_key`);
ALTER TABLE `admin_logs` ADD PRIMARY KEY (`id`);
ALTER TABLE `attachments` ADD PRIMARY KEY (`id`), ADD KEY `idx_attachments_parent` (`parent_type`,`parent_id`);
ALTER TABLE `courses` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `name` (`name`), ADD UNIQUE KEY `abbreviation` (`abbreviation`);
ALTER TABLE `course_attendance` ADD PRIMARY KEY (`user_id`,`course_id`), ADD KEY `course_id` (`course_id`);
ALTER TABLE `events` ADD PRIMARY KEY (`id`), ADD KEY `fk_event_leader` (`leader_user_id`);
ALTER TABLE `event_assignments` ADD PRIMARY KEY (`assignment_id`), ADD UNIQUE KEY `unique_assignment` (`event_id`,`user_id`), ADD KEY `user_id` (`user_id`);
ALTER TABLE `event_attendance` ADD PRIMARY KEY (`user_id`,`event_id`), ADD KEY `event_id` (`event_id`);
ALTER TABLE `event_chat_messages` ADD PRIMARY KEY (`id`), ADD KEY `event_id` (`event_id`), ADD KEY `event_chat_messages_ibfk_2` (`user_id`);
ALTER TABLE `event_custom_fields` ADD PRIMARY KEY (`id`), ADD KEY `event_id` (`event_id`);
ALTER TABLE `event_custom_field_responses` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `field_id` (`field_id`,`user_id`), ADD KEY `user_id` (`user_id`);
ALTER TABLE `event_skill_requirements` ADD PRIMARY KEY (`id`), ADD KEY `event_id` (`event_id`), ADD KEY `required_course_id` (`required_course_id`);
ALTER TABLE `event_storage_reservations` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `unique_event_item` (`event_id`,`item_id`), ADD KEY `item_id` (`item_id`);
ALTER TABLE `event_tasks` ADD PRIMARY KEY (`id`), ADD KEY `event_id` (`event_id`);
ALTER TABLE `event_task_assignments` ADD PRIMARY KEY (`task_id`,`user_id`), ADD KEY `user_id` (`user_id`);
ALTER TABLE `event_task_kits` ADD PRIMARY KEY (`task_id`,`kit_id`), ADD KEY `kit_id` (`kit_id`);
ALTER TABLE `event_task_storage_items` ADD PRIMARY KEY (`task_id`,`item_id`), ADD KEY `item_id` (`item_id`);
ALTER TABLE `feedback_forms` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `event_id_unique` (`event_id`);
ALTER TABLE `feedback_responses` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `form_user_unique` (`form_id`,`user_id`), ADD KEY `user_id` (`user_id`);
ALTER TABLE `feedback_submissions` ADD PRIMARY KEY (`id`), ADD KEY `user_id` (`user_id`);
ALTER TABLE `files` ADD PRIMARY KEY (`id`), ADD KEY `category_id` (`category_id`);
ALTER TABLE `file_categories` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `name` (`name`);
ALTER TABLE `inventory_kits` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `name` (`name`);
ALTER TABLE `inventory_kit_items` ADD PRIMARY KEY (`kit_id`,`item_id`), ADD KEY `item_id` (`item_id`);
ALTER TABLE `maintenance_log` ADD PRIMARY KEY (`id`), ADD KEY `item_id` (`item_id`), ADD KEY `user_id` (`user_id`);
ALTER TABLE `meetings` ADD PRIMARY KEY (`id`), ADD KEY `course_id` (`course_id`), ADD KEY `fk_meeting_leader` (`leader_user_id`);
ALTER TABLE `meeting_attendance` ADD PRIMARY KEY (`user_id`,`meeting_id`), ADD KEY `meeting_id` (`meeting_id`);
ALTER TABLE `permissions` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `permission_key_unique` (`permission_key`);
ALTER TABLE `profile_change_requests` ADD PRIMARY KEY (`id`), ADD KEY `user_id` (`user_id`), ADD KEY `status` (`status`), ADD KEY `profile_change_requests_ibfk_2` (`reviewed_by_admin_id`);
ALTER TABLE `roles` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `role_name_unique` (`role_name`);
ALTER TABLE `shared_documents` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `document_name` (`document_name`), ADD UNIQUE KEY `idx_doc_name` (`document_name`);
ALTER TABLE `storage_items` ADD PRIMARY KEY (`id`), ADD KEY `fk_holder_user` (`current_holder_user_id`), ADD KEY `fk_assigned_event` (`assigned_event_id`);
ALTER TABLE `storage_log` ADD PRIMARY KEY (`id`), ADD KEY `item_id` (`item_id`), ADD KEY `user_id` (`user_id`), ADD KEY `fk_log_event` (`event_id`);
ALTER TABLE `users` ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `username` (`username`), ADD UNIQUE KEY `email` (`email`), ADD KEY `fk_user_role` (`role_id`);
ALTER TABLE `user_achievements` ADD PRIMARY KEY (`user_id`,`achievement_id`), ADD KEY `achievement_id` (`achievement_id`);
ALTER TABLE `user_passkeys` ADD PRIMARY KEY (`id`), ADD KEY `user_id` (`user_id`);
ALTER TABLE `user_permissions` ADD PRIMARY KEY (`user_id`,`permission_id`), ADD KEY `permission_id` (`permission_id`);
ALTER TABLE `user_qualifications` ADD PRIMARY KEY (`user_id`,`course_id`), ADD KEY `course_id` (`course_id`);

-- ===================================================================
-- PART 3: ADD ALL AUTO_INCREMENT SETTINGS
-- ===================================================================

ALTER TABLE `achievements` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `admin_logs` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `attachments` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `courses` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `events` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `event_assignments` MODIFY `assignment_id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `event_chat_messages` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `event_custom_fields` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `event_custom_field_responses` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `event_skill_requirements` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `event_storage_reservations` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `event_tasks` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `feedback_forms` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `feedback_responses` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `feedback_submissions` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `files` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `file_categories` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `inventory_kits` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `maintenance_log` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `meetings` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `permissions` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `profile_change_requests` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `roles` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `shared_documents` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `storage_items` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `storage_log` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `users` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `user_passkeys` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

-- ===================================================================
-- PART 4: ADD ALL FOREIGN KEY CONSTRAINTS
-- ===================================================================

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