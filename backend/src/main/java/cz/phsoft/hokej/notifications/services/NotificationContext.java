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
 *
 * Třída je neměnná, instance se vytváří prostřednictvím vnořeného
 * builderu {@link NotificationContext.Builder}.
 */
public class NotificationContext {

    /**
     * Hráč, kterého se notifikace týká.
     */
    private final PlayerEntity player;

    /**
     * Uživatel, ke kterému hráč patří.
     */
    private final AppUserEntity user;

    /**
     * Zápas, k němuž se notifikovaná událost vztahuje.
     */
    private final MatchEntity match;

    /**
     * Konkrétní registrace hráče na zápas, pokud je pro notifikaci relevantní.
     */
    private final MatchRegistrationEntity registration;

    /**
     * Soukromý konstruktor používaný builderem.
     *
     * Instance se vytváří pouze prostřednictvím {@link Builder},
     * což zajišťuje neměnnost a kontrolované skládání kontextu.
     *
     * @param b builder obsahující všechny nastavené hodnoty
     */
    private NotificationContext(Builder b) {
        this.player = b.player;
        this.user = b.user;
        this.match = b.match;
        this.registration = b.registration;
    }

    /**
     * Vrací hráče, kterého se notifikace týká.
     *
     * @return entita hráče nebo null, pokud nebyla nastavena
     */
    public PlayerEntity getPlayer() {
        return player;
    }

    /**
     * Vrací uživatele, ke kterému hráč patří.
     *
     * @return entita uživatele nebo null, pokud nebyla nastavena
     */
    public AppUserEntity getUser() {
        return user;
    }

    /**
     * Vrací zápas, k němuž se událost notifikace vztahuje.
     *
     * @return entita zápasu nebo null, pokud nebyla nastavena
     */
    public MatchEntity getMatch() {
        return match;
    }

    /**
     * Vrací registraci hráče na zápas, pokud je k dispozici.
     *
     * @return entita registrace nebo null, pokud nebyla nastavena
     */
    public MatchRegistrationEntity getRegistration() {
        return registration;
    }

    // Builder třídy

    /**
     * Builder pro vytvoření instance NotificationContext.
     *
     * Umožňuje postupné skládání kontextu podle potřeby
     * konkrétní notifikace. Jednotlivé metody nastavují
     * části kontextu a vracejí odkaz na builder pro řetězení.
     */
    public static class Builder {
        private PlayerEntity player;
        private AppUserEntity user;
        private MatchEntity match;
        private MatchRegistrationEntity registration;

        /**
         * Nastaví hráče, kterého se notifikace týká.
         *
         * @param player entita hráče
         * @return tento builder pro další řetězení volání
         */
        public Builder player(PlayerEntity player) {
            this.player = player;
            return this;
        }

        /**
         * Nastaví uživatele, ke kterému hráč patří.
         *
         * @param user entita uživatele
         * @return tento builder pro další řetězení volání
         */
        public Builder user(AppUserEntity user) {
            this.user = user;
            return this;
        }

        /**
         * Nastaví zápas, k němuž se událost vztahuje.
         *
         * @param match entita zápasu
         * @return tento builder pro další řetězení volání
         */
        public Builder match(MatchEntity match) {
            this.match = match;
            return this;
        }

        /**
         * Nastaví registraci hráče na zápas.
         *
         * @param registration entita registrace hráče na zápas
         * @return tento builder pro další řetězení volání
         */
        public Builder registration(MatchRegistrationEntity registration) {
            this.registration = registration;
            return this;
        }

        /**
         * Sestaví novou instanci NotificationContext.
         *
         * Využije aktuální stav builderu a vrátí neměnný
         * kontextový objekt připravený k předání do builderů
         * notifikací.
         *
         * @return nová instance NotificationContext
         */
        public NotificationContext build() {
            return new NotificationContext(this);
        }
    }
}