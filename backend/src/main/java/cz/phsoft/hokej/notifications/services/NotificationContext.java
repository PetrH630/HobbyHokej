package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.registration.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;

/**
 * Typový kontejner pro notifikační data.
 *
 * Slouží k předání všech relevantních dat do builderů notifikací
 * (email a SMS). Umožňuje sjednotit vstupní parametry tak,
 * aby jednotlivé buildery nemusely pracovat s množstvím
 * volných parametrů.
 *
 * Typicky obsahuje:
 * - hráče, kterého se notifikace týká,
 * - uživatele, ke kterému hráč patří,
 * - zápas, k němuž se událost vztahuje,
 * - konkrétní registraci, pokud je relevantní.
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

    // Builder třídy

    /**
     * Builder pro vytvoření instance NotificationContext.
     *
     * Umožňuje postupné skládání kontextu podle potřeby
     * konkrétní notifikace.
     */
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
