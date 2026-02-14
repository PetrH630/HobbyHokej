ALTER TABLE `app_users`
  ADD UNIQUE KEY `UK_4vj92ux8a2eehds1mdvmks473` (`email`);

ALTER TABLE `app_user_settings`
  ADD UNIQUE KEY `UK_3t96bi6n25bfd4r8lslntdp9w` (`user_id`);

ALTER TABLE `email_verification_tokens`
  ADD UNIQUE KEY `UK_ewmvysc7e9y6uy7og2c21axa9` (`token`),
  ADD UNIQUE KEY `UK_s3mje1c85ftmp2uld6dt1bffs` (`user_id`);

ALTER TABLE `forgotten_password_reset_token_entity`
  ADD UNIQUE KEY `UK_bxs8l2j2yqakm3mfjnu6c2g7p` (`token`),
  ADD KEY `FKef2aoyyhsebwhnpo566jq8w05` (`user_id`);

ALTER TABLE `matches`
  ADD KEY `FKt32wb9qvxjjj8rtbciiwurdyv` (`season_id`);

ALTER TABLE `match_registrations`
  ADD KEY `FKd7lcwyhhhjy7u4ppp869sa5pv` (`match_id`),
  ADD KEY `FKqbhngy9958kbv067yvhiqsiav` (`player_id`);

ALTER TABLE `player_entity`
  ADD KEY `FKj2ej4242t8hsfhexwr1k3iybk` (`user_id`);

ALTER TABLE `player_inactivity_period`
  ADD KEY `FKlliyj59kukydf1eubv8ab5bob` (`player_id`);

ALTER TABLE `player_settings`
  ADD UNIQUE KEY `UK_f097cc1eamfljp30kp8ithf0t` (`player_id`);
