-- Flyway migration V33: Add table for event task dependencies

CREATE TABLE `event_task_dependencies` (
    `task_id` INT NOT NULL,
    `depends_on_task_id` INT NOT NULL,
    PRIMARY KEY (`task_id`, `depends_on_task_id`),
    FOREIGN KEY (`task_id`) REFERENCES `event_tasks`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`depends_on_task_id`) REFERENCES `event_tasks`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;