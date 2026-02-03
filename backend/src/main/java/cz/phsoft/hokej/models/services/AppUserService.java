package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.models.dto.AppUserDTO;
import cz.phsoft.hokej.models.dto.ForgottenPasswordResetDTO;
import cz.phsoft.hokej.models.dto.RegisterUserDTO;

import java.util.List;

/**
 * Rozhraní pro správu uživatelských účtů aplikace.
 *
 * Definuje se smlouva pro registraci uživatelů, změnu a reset hesla,
 * aktivaci nebo deaktivaci účtů a čtení seznamů uživatelů. Rozhraní
 * se používá v controller vrstvách a v dalších service třídách.
 *
 * Implementace pracuje s DTO objekty a zajišťuje napojení
 * na perzistenční vrstvu (repozitáře) a notifikační mechanismy.
 */
public interface AppUserService {

    /**
     * Zaregistruje nového uživatele do systému.
     *
     * Provádí se kontrola jedinečnosti emailu, shody hesla a jeho potvrzení
     * a další validační pravidla. Nový účet se typicky zakládá jako neaktivní
     * a jeho aktivace se dokončuje přes aktivační odkaz.
     *
     * @param registerUserDTO registrační data nového uživatele
     */
    void register(RegisterUserDTO registerUserDTO);

    /**
     * Aktualizuje údaje uživatele podle emailu.
     *
     * Používá se pro změny běžných údajů (jméno, příjmení, email).
     * Bezpečnost a oprávnění se kontrolují v controller vrstvě.
     *
     * @param email email uživatele, který má být aktualizován
     * @param dto   nové hodnoty uživatelských údajů
     */
    void updateUser(String email, AppUserDTO dto);

    /**
     * Vrací aktuálně přihlášeného uživatele podle emailu.
     *
     * Metoda se používá zejména v kontextu endpointů typu "/me".
     *
     * @param email email přihlášeného uživatele
     * @return DTO reprezentace aktuálního uživatele
     */
    AppUserDTO getCurrentUser(String email);

    /**
     * Vrací seznam všech uživatelů v systému.
     *
     * Typicky se používá v administrátorském rozhraní
     * pro správu uživatelů.
     *
     * @return seznam uživatelů ve formě DTO
     */
    List<AppUserDTO> getAllUsers();

    /**
     * Vrací uživatele podle ID.
     *
     * Používá se v administraci, kde je potřeba pracovat
     * s konkrétním účtem podle jeho identifikátoru.
     *
     * @param userId ID uživatele
     * @return uživatel ve formě DTO
     */
    AppUserDTO getUserById(Long userId);

    /**
     * Změní heslo uživatele.
     *
     * Ověřuje se správnost původního hesla, shoda nového hesla
     * a jeho potvrzení a případné bezpečnostní požadavky.
     *
     * @param email              email uživatele
     * @param oldPassword        původní heslo
     * @param newPassword        nové heslo
     * @param newPasswordConfirm potvrzení nového hesla
     */
    void changePassword(
            String email,
            String oldPassword,
            String newPassword,
            String newPasswordConfirm
    );

    /**
     * Resetuje heslo uživatele na výchozí hodnotu.
     *
     * Operace se typicky používá v administraci, kde se řeší
     * problémy s přihlášením. Konkrétní politika bezpečného
     * zacházení s takovým heslem se řeší v aplikační logice.
     *
     * @param userId ID uživatele, jehož heslo má být resetováno
     */
    void resetPassword(Long userId);

    /**
     * Aktivuje uživatelský účet na základě aktivačního tokenu.
     *
     * Metoda se používá po registraci uživatele, kdy je odkaz
     * zaslán v aktivačním emailu. Při úspěchu se účet označí
     * jako povolený k přihlášení.
     *
     * @param token aktivační token
     * @return true, pokud byla aktivace úspěšná, jinak false
     */
    boolean activateUser(String token);

    /**
     * Aktivuje uživatelský účet v administraci.
     *
     * Aktivace se provádí bez použití aktivačního tokenu,
     * typicky v situaci, kdy má uživatel technický problém
     * s aktivačním emailem.
     *
     * @param id ID uživatele
     */
    void activateUserByAdmin(Long id);

    /**
     * Deaktivuje uživatelský účet v administraci.
     *
     * Účet se ponechá v databázi, ale uživatel se nemůže
     * dočasně přihlásit do aplikace.
     *
     * @param id ID uživatele
     */
    void deactivateUserByAdmin(Long id);

    /**
     * Vytvoří požadavek na reset zapomenutého hesla.
     *
     * Pro daný email se vygeneruje resetovací token a odešle
     * se odpovídající notifikace (například email s odkazem
     * na formulář pro nastavení nového hesla).
     *
     * @param email email uživatele
     */
    void requestForgottenPasswordReset(String email);

    /**
     * Vrací email uživatele pro zadaný resetovací token.
     *
     * Metoda se používá při načítání formuláře pro nastavení
     * nového hesla, aby se ověřilo, ke kterému účtu token patří.
     *
     * @param token resetovací token
     * @return email uživatele
     */
    String getForgottenPasswordResetEmail(String token);

    /**
     * Nastaví nové heslo na základě tokenu pro zapomenuté heslo.
     *
     * Token se ověří, zkontroluje se shoda hesla a jeho potvrzení
     * a poté se heslo uloží v zahashované podobě.
     *
     * @param dto data pro reset zapomenutého hesla
     */
    void forgottenPasswordReset(ForgottenPasswordResetDTO dto);
}
