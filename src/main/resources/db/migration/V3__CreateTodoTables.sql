CREATE TABLE todo_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    display_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE todo_tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    content TEXT NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_todo_category
        FOREIGN KEY (category_id)
        REFERENCES todo_categories(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_todo_categories_display_order ON todo_categories(display_order);
CREATE INDEX idx_todo_tasks_display_order ON todo_tasks(display_order);