package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;

/**
 * Typový kontejner pro notifikační data.
 *
 * Slouží k předání všech relevantních dat
 * do builderů notifikací (email / SMS).
 */
public class NotificationContext {

    private final PlayerEntity player;
    private final AppUserEntity user;
    private final MatchEntity match;
    private final MatchRegistrationEntity registration;

    private NotificationContext(Builder b) {
        this.player = b.player;
        this.user = b.user;
        this.match = b.match;
        this.registration = b.registration;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public AppUserEntity getUser() {
        return user;
    }

    public MatchEntity getMatch() {
        return match;
    }

    public MatchRegistrationEntity getRegistration() {
        return registration;
    }

    // -------------------------
    // BUILDER
    // -------------------------
    public static class Builder {
        private PlayerEntity player;
        private AppUserEntity user;
        private MatchEntity match;
        private MatchRegistrationEntity registration;

        public Builder player(PlayerEntity player) {
            this.player = player;
            return this;
        }

        public Builder user(AppUserEntity user) {
            this.user = user;
            return this;
        }

        public Builder match(MatchEntity match) {
            this.match = match;
            return this;
        }

        public Builder registration(MatchRegistrationEntity registration) {
            this.registration = registration;
            return this;
        }

        public NotificationContext build() {
            return new NotificationContext(this);
        }
    }
}
