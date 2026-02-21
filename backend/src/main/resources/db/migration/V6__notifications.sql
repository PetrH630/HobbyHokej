CREATE TABLE `notifications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `player_id` bigint(20) DEFAULT NULL,
  `type` varchar(100) NOT NULL,
  `message_short` varchar(255) NOT NULL,
  `message_full` varchar(2000) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `read_at` datetime(6) DEFAULT NULL,
  `created_by_user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notification_user` (`user_id`),
  KEY `idx_notification_created_at` (`created_at`),
  KEY `idx_notification_read_at` (`read_at`),
  CONSTRAINT `FK_notification_user`
    FOREIGN KEY (`user_id`) REFERENCES `app_users` (`id`),
  CONSTRAINT `FK_notification_player`
    FOREIGN KEY (`player_id`) REFERENCES `player_entity` (`id`),
  CONSTRAINT `FK_notification_created_by`
    FOREIGN KEY (`created_by_user_id`) REFERENCES `app_users` (`id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;