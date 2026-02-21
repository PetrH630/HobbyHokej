CREATE TABLE `app_users` (
  `id` bigint(20) NOT NULL,
  `email` varchar(255) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ROLE_PLAYER','ROLE_MANAGER','ROLE_ADMIN') NOT NULL,
  `surname` varchar(255) NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `current_login_at` datetime(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `app_users_history` (
  `id` bigint(20) NOT NULL,
  `action` varchar(255) NOT NULL,
  `changed_at` datetime(6) NOT NULL,
  `email` varchar(255) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `original_timestamp` datetime(6) NOT NULL,
  `role` enum('ROLE_PLAYER','ROLE_MANAGER','ROLE_ADMIN') NOT NULL,
  `surname` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `app_user_settings` (
  `id` bigint(20) NOT NULL,
  `copy_all_player_notifications_to_user_email` bit(1) NOT NULL,
  `default_landing_page` varchar(50) DEFAULT NULL,
  `email_digest_enabled` bit(1) NOT NULL,
  `email_digest_time` time(6) DEFAULT NULL,
  `global_notification_level` enum('ALL','IMPORTANT_ONLY','NONE') NOT NULL,
  `manager_notification_level` enum('ALL','IMPORTANT_ONLY','NONE') DEFAULT NULL,
  `player_selection_mode` enum('FIRST_PLAYER','ALWAYS_CHOOSE') NOT NULL,
  `receive_notifications_for_players_with_own_email` bit(1) NOT NULL,
  `timezone` varchar(50) DEFAULT NULL,
  `ui_language` varchar(10) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `email_verification_tokens` (
  `id` bigint(20) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `token` varchar(64) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `forgotten_password_reset_token_entity` (
  `id` bigint(20) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `token` varchar(128) NOT NULL,
  `used_at` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `matches` (
  `id` bigint(20) NOT NULL,
  `cancel_reason` enum('NOT_ENOUGH_PLAYERS','TECHNICAL_ISSUE','WEATHER','ORGANIZER_DECISION','OTHER') DEFAULT NULL,
  `created_by_user_id` bigint(20) DEFAULT NULL,
  `date_time` datetime(6) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `last_modified_by_user_id` bigint(20) DEFAULT NULL,
  `location` varchar(255) NOT NULL,
  `match_status` enum('UNCANCELED','CANCELED','UPDATED') DEFAULT NULL,
  `max_players` int(11) NOT NULL,
  `price` int(11) NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `season_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `matches_history` (
  `id` bigint(20) NOT NULL,
  `action` varchar(255) NOT NULL,
  `cancel_reason` enum('NOT_ENOUGH_PLAYERS','TECHNICAL_ISSUE','WEATHER','ORGANIZER_DECISION','OTHER') DEFAULT NULL,
  `changed_at` datetime(6) NOT NULL,
  `created_by_user_id` bigint(20) DEFAULT NULL,
  `date_time` datetime(6) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `last_modified_by_user_id` bigint(20) DEFAULT NULL,
  `location` varchar(255) NOT NULL,
  `match_id` bigint(20) NOT NULL,
  `match_status` enum('UNCANCELED','CANCELED','UPDATED') DEFAULT NULL,
  `max_players` int(11) NOT NULL,
  `original_timestamp` datetime(6) NOT NULL,
  `price` int(11) NOT NULL,
  `season_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `match_registrations` (
  `id` bigint(20) NOT NULL,
  `admin_note` varchar(255) DEFAULT NULL,
  `created_by` varchar(255) NOT NULL,
  `excuse_note` varchar(255) DEFAULT NULL,
  `excuse_reason` enum('NEMOC','PRACE','NECHE_SE_MI','JINE') DEFAULT NULL,
  `status` enum('REGISTERED','UNREGISTERED','EXCUSED','RESERVED','NO_RESPONSE','SUBSTITUTE','NO_EXCUSED') NOT NULL,
  `team` enum('DARK','LIGHT') DEFAULT NULL,
  `timestamp` datetime(6) NOT NULL,
  `match_id` bigint(20) NOT NULL,
  `player_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `match_registration_history` (
  `id` bigint(20) NOT NULL,
  `action` varchar(255) NOT NULL,
  `admin_note` varchar(255) DEFAULT NULL,
  `changed_at` datetime(6) NOT NULL,
  `created_by` varchar(255) NOT NULL,
  `excuse_note` varchar(255) DEFAULT NULL,
  `excuse_reason` enum('NEMOC','PRACE','NECHE_SE_MI','JINE') DEFAULT NULL,
  `match_id` bigint(20) NOT NULL,
  `match_registration_id` bigint(20) NOT NULL,
  `original_timestamp` datetime(6) NOT NULL,
  `player_id` bigint(20) NOT NULL,
  `status` enum('REGISTERED','UNREGISTERED','EXCUSED','RESERVED','NO_RESPONSE','SUBSTITUTE','NO_EXCUSED') NOT NULL,
  `team` enum('DARK','LIGHT') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `player_entity` (
  `id` bigint(20) NOT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `nickname` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `player_status` enum('PENDING','APPROVED','REJECTED') NOT NULL,
  `surname` varchar(255) NOT NULL,
  `team` enum('DARK','LIGHT') DEFAULT NULL,
  `timestamp` datetime(6) NOT NULL,
  `type` enum('VIP','STANDARD','BASIC') NOT NULL,
  `user_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `player_entity_history` (
  `id` bigint(20) NOT NULL,
  `action` varchar(255) NOT NULL,
  `changed_at` datetime(6) NOT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `nickname` varchar(255) DEFAULT NULL,
  `original_timestamp` datetime(6) NOT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `player_id` bigint(20) NOT NULL,
  `player_status` enum('PENDING','APPROVED','REJECTED') NOT NULL,
  `surname` varchar(255) NOT NULL,
  `team` enum('DARK','LIGHT') DEFAULT NULL,
  `type` enum('VIP','STANDARD','BASIC') NOT NULL,
  `user_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `player_inactivity_period` (
  `id` bigint(20) NOT NULL,
  `inactive_from` datetime(6) NOT NULL,
  `inactive_to` datetime(6) NOT NULL,
  `inactivity_reason` varchar(255) NOT NULL,
  `player_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `player_settings` (
  `id` bigint(20) NOT NULL,
  `contact_email` varchar(255) DEFAULT NULL,
  `contact_phone` varchar(50) DEFAULT NULL,
  `email_enabled` bit(1) NOT NULL,
  `notify_on_excuse` bit(1) NOT NULL,
  `notify_on_match_cancel` bit(1) NOT NULL,
  `notify_on_match_change` bit(1) NOT NULL,
  `notify_on_payment` bit(1) NOT NULL,
  `notify_on_registration` bit(1) NOT NULL,
  `notify_reminders` bit(1) NOT NULL,
  `reminder_hours_before` int(11) DEFAULT NULL,
  `sms_enabled` bit(1) NOT NULL,
  `player_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `season` (
  `id` bigint(20) NOT NULL,
  `active` bit(1) NOT NULL,
  `created_by_user_id` bigint(20) DEFAULT NULL,
  `end_date` date NOT NULL,
  `name` varchar(255) NOT NULL,
  `start_date` date NOT NULL,
  `timestamp` datetime(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `season_history` (
  `id` bigint(20) NOT NULL,
  `action` varchar(255) NOT NULL,
  `active` bit(1) NOT NULL,
  `changed_at` datetime(6) NOT NULL,
  `created_by_user_id` bigint(20) DEFAULT NULL,
  `end_date` date NOT NULL,
  `name` varchar(255) NOT NULL,
  `original_timestamp` datetime(6) NOT NULL,
  `season_id` bigint(20) NOT NULL,
  `start_date` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
