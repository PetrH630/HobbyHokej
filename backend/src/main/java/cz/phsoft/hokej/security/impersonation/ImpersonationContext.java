package cz.phsoft.hokej.security.impersonation;

/**
 * Kontext pro práci s impersonací hráče.
 *
 * Třída se používá pro dočasné uložení identifikátoru hráče,
 * za kterého aktuálně přihlášený uživatel vystupuje. Kontext je
 * uložen pomocí ThreadLocal, takže hodnota je vázána na konkrétní
 * vlákno zpracovávající požadavek.
 *
 * Typickým použitím je administrátorská funkce, kdy administrátor
 * nebo manažer dočasně přebírá identitu hráče pro účely testování
 * nebo kontroly chování aplikace.
 *
 * Třída je statická utilita bez instancí. Životnost hodnoty je
 * omezená na dobu zpracování jednoho požadavku a musí být po jeho
 * dokončení vyčištěna voláním metody clear.
 */
public final class ImpersonationContext {

    /**
     * ThreadLocal úložiště pro identifikátor impersonovaného hráče.
     */
    private static final ThreadLocal<Long> IMPERSONATED_PLAYER_ID = new ThreadLocal<>();

    private ImpersonationContext() {
    }

    /**
     * Nastaví identifikátor hráče, za kterého se má aktuálně vystupovat.
     *
     * Hodnota se uloží do ThreadLocal kontextu a je dostupná
     * po dobu zpracování aktuálního vlákna.
     *
     * @param playerId Identifikátor hráče, který má být impersonován.
     */
    public static void setImpersonatedPlayerId(Long playerId) {
        IMPERSONATED_PLAYER_ID.set(playerId);
    }

    /**
     * Vrátí identifikátor aktuálně impersonovaného hráče.
     *
     * Pokud impersonace není aktivní, vrací se null.
     *
     * @return Identifikátor impersonovaného hráče, nebo null pokud není nastavena.
     */
    public static Long getImpersonatedPlayerId() {
        return IMPERSONATED_PLAYER_ID.get();
    }

    /**
     * Vyhodnocuje, zda je aktuálně aktivní impersonace.
     *
     * Impersonace je považována za aktivní, pokud je v kontextu
     * nastavena hodnota identifikátoru hráče.
     *
     * @return True, pokud je impersonace aktivní, jinak false.
     */
    public static boolean isImpersonating() {
        return getImpersonatedPlayerId() != null;
    }

    /**
     * Vyčistí kontext impersonace.
     *
     * Metoda odstraní hodnotu z ThreadLocal úložiště.
     * Musí být volána po dokončení požadavku, aby nedošlo
     * k úniku kontextu mezi jednotlivými požadavky.
     */
    public static void clear() {
        IMPERSONATED_PLAYER_ID.remove();
    }
}
