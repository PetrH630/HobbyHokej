ALTER TABLE `app_user_settings`
  ADD CONSTRAINT `FKe1p8ht9pl78l26s3x7rd92ycs`
  FOREIGN KEY (`user_id`) REFERENCES `app_users` (`id`);

ALTER TABLE `email_verification_tokens`
  ADD CONSTRAINT `FK3p1m42k50qhjbo815nl38nfby`
  FOREIGN KEY (`user_id`) REFERENCES `app_users` (`id`);

ALTER TABLE `forgotten_password_reset_token_entity`
  ADD CONSTRAINT `FKef2aoyyhsebwhnpo566jq8w05`
  FOREIGN KEY (`user_id`) REFERENCES `app_users` (`id`);

ALTER TABLE `matches`
  ADD CONSTRAINT `FKt32wb9qvxjjj8rtbciiwurdyv`
  FOREIGN KEY (`season_id`) REFERENCES `season` (`id`);

ALTER TABLE `match_registrations`
  ADD CONSTRAINT `FKd7lcwyhhhjy7u4ppp869sa5pv`
  FOREIGN KEY (`match_id`) REFERENCES `matches` (`id`),
  ADD CONSTRAINT `FKqbhngy9958kbv067yvhiqsiav`
  FOREIGN KEY (`player_id`) REFERENCES `player_entity` (`id`);

ALTER TABLE `player_entity`
  ADD CONSTRAINT `FKj2ej4242t8hsfhexwr1k3iybk`
  FOREIGN KEY (`user_id`) REFERENCES `app_users` (`id`);

ALTER TABLE `player_inactivity_period`
  ADD CONSTRAINT `FKlliyj59kukydf1eubv8ab5bob`
  FOREIGN KEY (`player_id`) REFERENCES `player_entity` (`id`);

ALTER TABLE `player_settings`
  ADD CONSTRAINT `FKg331s5addqswg72a9dlldy2qc`
  FOREIGN KEY (`player_id`) REFERENCES `player_entity` (`id`);
