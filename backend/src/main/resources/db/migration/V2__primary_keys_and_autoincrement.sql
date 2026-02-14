ALTER TABLE `app_users`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `app_users_history`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `app_user_settings`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `email_verification_tokens`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `forgotten_password_reset_token_entity`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `matches`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `matches_history`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `match_registrations`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `match_registration_history`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `player_entity`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `player_entity_history`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `player_inactivity_period`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `player_settings`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `season`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `season_history`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `app_users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `app_users_history`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `app_user_settings`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `email_verification_tokens`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `forgotten_password_reset_token_entity`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `matches`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `matches_history`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `match_registrations`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `match_registration_history`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `player_entity`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `player_entity_history`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `player_inactivity_period`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `player_settings`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `season`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `season_history`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;
