package cz.phsoft.hokej.security;

/**
 * Centrální definice klíčů používaných v HTTP session.
 * <p>
 * Tato třída slouží jako jednotné místo pro uložení názvů
 * session atributů, aby se zabránilo:
 * </p>
 * <ul>
 *     <li>duplicitám řetězcových konstant,</li>
 *     <li>překlepům v názvech klíčů,</li>
 *     <li>nekonzistenci napříč aplikací.</li>
 * </ul>
 *
 * Použití:
 * <ul>
 *     <li>využívá se ve službách pracujících s HTTP session,</li>
 *     <li>typicky v {@code CurrentPlayerService} a souvisejících třídách.</li>
 * </ul>
 *
 * Technická poznámka:
 * <ul>
 *     <li>třída je {@code final} a má privátní konstruktor,</li>
 *     <li>slouží výhradně jako držák konstant (utility class).</li>
 * </ul>
 */
public final class SessionKeys {

    /**
     * Klíč session atributu pro ID aktuálně zvoleného hráče.
     * <p>
     * Hodnota představuje identifikátor hráče, se kterým
     * aktuálně pracuje přihlášený uživatel.
     * </p>
     */
    public static final String CURRENT_PLAYER_ID = "CURRENT_PLAYER_ID";

    /**
     * Privátní konstruktor zabraňující vytvoření instance třídy.
     */
    private SessionKeys() {
    }
}
