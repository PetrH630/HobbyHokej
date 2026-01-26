package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.models.dto.AppUserDTO;
import cz.phsoft.hokej.models.dto.RegisterUserDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Rozhraní pro správu uživatelských účtů v aplikaci.
 * <p>
 * Definuje kontrakt pro práci s uživateli z pohledu business logiky.
 * Implementace tohoto rozhraní zajišťuje registraci, správu účtů,
 * změnu hesla a aktivaci uživatelů.
 * </p>
 *
 * Účel:
 * <ul>
 *     <li>oddělit business logiku práce s uživateli od technické implementace,</li>
 *     <li>poskytnout jednotný vstupní bod pro správu uživatelů,</li>
 *     <li>umožnit bezpečnou a konzistentní správu uživatelských účtů.</li>
 * </ul>
 *
 * Použití:
 * <ul>
 *     <li>využívá se v controllerech a dalších business službách,</li>
 *     <li>pracuje výhradně s DTO objekty, nikoliv s entitami.</li>
 * </ul>
 *
 * Implementační poznámky:
 * <ul>
 *     <li>implementace by měla řešit validace vstupních dat,</li>
 *     <li>bezpečnostní kontroly (hesla, tokeny, role),</li>
 *     <li>správu životního cyklu uživatelského účtu.</li>
 * </ul>
 */
public interface AppUserService {

    /**
     * Zaregistruje nového uživatele do systému.
     * <p>
     * Metoda vytvoří nový uživatelský účet na základě
     * registračních dat poskytnutých uživatelem.
     * </p>
     *
     * Validace:
     * <ul>
     *     <li>email musí být jedinečný,</li>
     *     <li>heslo a potvrzení hesla se musí shodovat,</li>
     *     <li>registrační data musí splňovat validační pravidla.</li>
     * </ul>
     *
     * @param registerUserDTO data potřebná pro registraci uživatele
     * @throws IllegalArgumentException pokud email již existuje
     *                                  nebo hesla nejsou shodná
     */
    void register(RegisterUserDTO registerUserDTO);
    /**
     * Aktualizuje údaje uživatele.
     * <p>
     * Metoda umožňuje změnu uživatelských údajů,
     * které nevyžadují změnu hesla (např. jméno, role, stav).
     * </p>
     *
     * @param email email uživatele, který má být aktualizován
     * @param dto   nové hodnoty uživatelských údajů
     */
    void updateUser(String email, AppUserDTO dto);
    /**
     * Vrátí aktuálně přihlášeného uživatele podle emailu.
     * <p>
     * Typicky se používá v kontextu přihlášeného uživatele
     * (např. endpointy typu {@code /me}).
     * </p>
     *
     * @param email email přihlášeného uživatele
     * @return DTO reprezentace aktuálního uživatele
     */
    AppUserDTO getCurrentUser(String email);

    /**
     * Vrátí seznam všech uživatelů v systému.
     * <p>
     * Typicky dostupné pouze pro administrátorské role.
     * </p>
     *
     * @return seznam uživatelů ve formě DTO
     */
    List<AppUserDTO> getAllUsers();

    /**
     * Změní heslo uživatele.
     * <p>
     * Metoda slouží pro standardní změnu hesla,
     * kdy uživatel zná své aktuální heslo.
     * </p>
     *
     * Validace:
     * <ul>
     *     <li>aktuální heslo musí odpovídat uloženému heslu,</li>
     *     <li>nové heslo a jeho potvrzení se musí shodovat,</li>
     *     <li>nové heslo musí splňovat bezpečnostní požadavky.</li>
     * </ul>
     *
     * @param email               email uživatele
     * @param oldPassword         aktuální heslo
     * @param newPassword         nové heslo
     * @param newPasswordConfirm  potvrzení nového hesla
     */
    void changePassword(
            String email,
            String oldPassword,
            String newPassword,
            String newPasswordConfirm
    );

    /**
     * Resetuje heslo uživatele.
     * <p>
     * Používá se typicky v administrátorském kontextu
     * nebo při řešení zapomenutého hesla.
     * </p>
     *
     * @param userId ID uživatele, jehož heslo má být resetováno
     */
    void resetPassword(Long userId);



    /**
     * Aktivuje uživatelský účet pomocí aktivačního tokenu.
     * <p>
     * Typicky se používá po registraci uživatele
     * (aktivace přes emailový odkaz).
     * </p>
     *
     * @param token aktivační token
     * @return {@code true}, pokud byla aktivace úspěšná,
     *         jinak {@code false}
     */
    boolean activateUser(String token);

    /**
     * Aktivuje uživatelský účet
     * <p>
     * Používá se po registraci uživatele
     * v administrátorském prostředí (uživatel má problémy s tokenem)
     * </p>
     */
   void activateUserByAdmin(Long id);
    /**
     * Deaktivuje uživatelský účet
     * <p>
     * Používá se v administrátorském prostředí
     * pro deaktivaci účtu,nechci uživatele mazat, ale nechci
     * aby měl dočasně přístup do aplikace.
     * </p>
     */
    void deactivateUserByAdmin(Long id);
}
