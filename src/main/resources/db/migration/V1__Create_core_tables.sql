-- Flyway migration V1: Create all tables without constraints.

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- ===================================================================
-- CREATE ALL TABLES
-- ===================================================================

CREATE TABLE `achievements` (
  `id` INT NOT NULL,
  `achievement_key` varchar(50) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `icon_class` varchar(50) DEFAULT 'fa-award'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `admin_logs` (
  `id` INT NOT NULL,
  `admin_username` varchar(50) DEFAULT NULL,
  `action_type` varchar(255) DEFAULT NULL,
  `details` text DEFAULT NULL,
  `status` ENUM('ACTIVE', 'REVOKED') NOT NULL DEFAULT 'ACTIVE',
  `context` JSON NULL DEFAULT NULL,
  `action_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `revoked_by_admin_id` INT NULL DEFAULT NULL,
  `revoked_at` TIMESTAMP NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `attachments` (
  `id` INT NOT NULL,
  `parent_type` enum('EVENT','MEETING') NOT NULL,
  `parent_id` INT NOT NULL,
  `filename` varchar(255) NOT NULL,
  `filepath` varchar(255) NOT NULL,
  `required_role` enum('NUTZER','ADMIN') NOT NULL DEFAULT 'NUTZER',
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `courses` (
  `id` INT NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `abbreviation` varchar(20) DEFAULT NULL,
  `description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `course_attendance` (
  `user_id` INT NOT NULL,
  `course_id` INT NOT NULL,
  `signup_status` enum('ANGEMELDET','ABGEMELDET') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `events` (
  `id` INT NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `event_datetime` datetime NOT NULL,
  `end_datetime` datetime DEFAULT NULL,
  `description` text DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `status` enum('GEPLANT','KOMPLETT','LAUFEND','ABGESCHLOSSEN','ABGESAGT') NOT NULL DEFAULT 'GEPLANT',
  `leader_user_id` INT DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_assignments` (
  `assignment_id` INT NOT NULL,
  `event_id` INT NOT NULL,
  `user_id` INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_attendance` (
  `user_id` INT NOT NULL,
  `event_id` INT NOT NULL,
  `signup_status` enum('ANGEMELDET','ABGEMELDET') NOT NULL,
  `commitment_status` enum('BESTÄTIGT','OFFEN','ZUGESAGT') NOT NULL DEFAULT 'OFFEN'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_chat_messages` (
  `id` INT NOT NULL,
  `event_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  `message_text` text DEFAULT NULL,
  `edited` BOOLEAN NOT NULL DEFAULT 0,
  `is_deleted` BOOLEAN NOT NULL DEFAULT 0,
  `deleted_by_user_id` INT DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `deleted_by_username` varchar(255) DEFAULT NULL,
  `sent_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_custom_fields` (
  `id` INT NOT NULL,
  `event_id` INT NOT NULL,
  `field_name` varchar(255) DEFAULT NULL,
  `field_type` enum('TEXT','BOOLEAN','DROPDOWN','CHECKBOX_GROUP') NOT NULL,
  `is_required` BOOLEAN NOT NULL DEFAULT 0,
  `field_options` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_custom_field_responses` (
  `id` INT NOT NULL,
  `field_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `response_value` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_skill_requirements` (
  `id` INT NOT NULL,
  `event_id` INT NOT NULL,
  `required_course_id` INT NOT NULL,
  `required_persons` INT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_storage_reservations` (
  `id` INT NOT NULL,
  `event_id` INT NOT NULL,
  `item_id` INT NOT NULL,
  `reserved_quantity` INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_tasks` (
  `id` INT NOT NULL,
  `event_id` INT NOT NULL,
  `description` text NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'OFFEN',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `display_order` INT NOT NULL DEFAULT 0,
  `required_persons` INT NOT NULL DEFAULT 0,
  `details` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_task_assignments` (
  `task_id` INT NOT NULL,
  `user_id` INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_task_kits` (
  `task_id` INT NOT NULL,
  `kit_id` INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_task_storage_items` (
  `task_id` INT NOT NULL,
  `item_id` INT NOT NULL,
  `quantity` INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `feedback_forms` (
  `id` INT NOT NULL,
  `event_id` INT NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `feedback_responses` (
  `id` INT NOT NULL,
  `form_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `rating` INT NOT NULL COMMENT 'e.g., 1 to 5 stars',
  `comments` text DEFAULT NULL,
  `submitted_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `feedback_submissions` (
  `id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `subject` varchar(255) NOT NULL,
  `display_title` varchar(255) DEFAULT NULL,
  `content` text NOT NULL,
  `submitted_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` enum('NEW','VIEWED','PLANNED','REJECTED','COMPLETED') NOT NULL DEFAULT 'NEW',
  `display_order` INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `files` (
  `id` INT NOT NULL,
  `filename` varchar(255) DEFAULT NULL,
  `filepath` varchar(512) DEFAULT NULL,
  `category_id` INT DEFAULT NULL,
  `required_role` varchar(20) DEFAULT NULL,
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `file_categories` (
  `id` INT NOT NULL,
  `name` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `inventory_kits` (
  `id` INT NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `inventory_kit_items` (
  `kit_id` INT NOT NULL,
  `item_id` INT NOT NULL,
  `quantity` INT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `login_attempts` (
  `username` varchar(50) NOT NULL,
  `attempts` INT NOT NULL DEFAULT 0,
  `last_attempt` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `maintenance_log` (
  `id` INT NOT NULL,
  `item_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `log_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `action` varchar(255) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `cost` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `meetings` (
  `id` INT NOT NULL,
  `course_id` INT NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `meeting_datetime` datetime NOT NULL,
  `end_datetime` datetime DEFAULT NULL,
  `leader_user_id` INT DEFAULT NULL,
  `description` text DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `meeting_attendance` (
  `user_id` INT NOT NULL,
  `meeting_id` INT NOT NULL,
  `attended` BOOLEAN DEFAULT 0,
  `remarks` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `permissions` (
  `id` INT NOT NULL,
  `permission_key` varchar(100) NOT NULL COMMENT 'e.g., USER_CREATE, EVENT_DELETE',
  `description` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `profile_change_requests` (
  `id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `requested_changes` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`requested_changes`)),
  `status` enum('PENDING','APPROVED','DENIED') NOT NULL DEFAULT 'PENDING',
  `requested_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `reviewed_by_admin_id` INT DEFAULT NULL,
  `reviewed_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `roles` (
  `id` INT NOT NULL,
  `role_name` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `shared_documents` (
  `id` INT NOT NULL,
  `document_name` varchar(100) NOT NULL,
  `content` text DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `storage_items` (
  `id` INT NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `location` varchar(50) DEFAULT NULL,
  `cabinet` varchar(50) DEFAULT NULL,
  `compartment` varchar(50) DEFAULT NULL,
  `quantity` INT NOT NULL DEFAULT 1,
  `max_quantity` INT NOT NULL DEFAULT 0,
  `defective_quantity` INT NOT NULL DEFAULT 0,
  `defect_reason` text DEFAULT NULL,
  `weight_kg` decimal(10,2) DEFAULT NULL,
  `price_eur` decimal(10,2) DEFAULT NULL,
  `image_path` varchar(512) DEFAULT NULL,
  `status` enum('IN_STORAGE','CHECKED_OUT','ASSIGNED_TO_EVENT','MAINTENANCE') NOT NULL DEFAULT 'IN_STORAGE',
  `current_holder_user_id` INT DEFAULT NULL,
  `assigned_event_id` INT DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `storage_log` (
  `id` INT NOT NULL,
  `item_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `quantity_change` INT NOT NULL,
  `notes` text DEFAULT NULL,
  `event_id` INT DEFAULT NULL,
  `transaction_timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `todo_categories` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `display_order` INT NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `todo_tasks` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `category_id` INT NOT NULL,
    `content` TEXT NOT NULL,
    `is_completed` BOOLEAN NOT NULL DEFAULT FALSE,
    `display_order` INT NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `users` (
  `id` INT NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `role_id` INT DEFAULT NULL,
  `class_year` INT DEFAULT NULL,
  `class_name` varchar(10) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `chat_color` varchar(7) DEFAULT '#E9ECEF',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `theme` varchar(10) DEFAULT 'light'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_achievements` (
  `user_id` INT NOT NULL,
  `achievement_id` INT NOT NULL,
  `earned_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_passkeys` (
  `id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `credential_id` text NOT NULL,
  `public_key` text NOT NULL,
  `signature_count` BIGINT NOT NULL,
  `user_handle` text NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_permissions` (
  `user_id` INT NOT NULL,
  `permission_id` INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_qualifications` (
  `user_id` INT NOT NULL,
  `course_id` INT NOT NULL,
  `completion_date` date DEFAULT NULL,
  `status` enum('BESUCHT','ABSOLVIERT', 'BESTANDEN', 'NICHT BESUCHT') NOT NULL DEFAULT 'BESUCHT',
  `remarks` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `wiki_documentation` (
  `id` INT NOT NULL,
  `file_path` varchar(512) NOT NULL,
  `content` longtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

COMMIT;