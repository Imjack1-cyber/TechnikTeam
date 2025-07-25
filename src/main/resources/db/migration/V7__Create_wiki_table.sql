CREATE TABLE `wiki_documentation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `file_path` varchar(512) NOT NULL,
  `content` longtext DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_wiki_file_path` (`file_path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;