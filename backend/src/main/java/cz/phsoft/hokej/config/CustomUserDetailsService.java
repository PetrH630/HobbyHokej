package cz.phsoft.hokej.config;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.exceptions.AccountNotActivatedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * {@link UserDetailsService} implementace napojující Spring Security
 * na vlastní databázový model uživatele ({@link AppUserEntity}).
 *
 * ÚČEL:
 * <ul>
 *     <li>při přihlášení načíst uživatele z DB podle emailu,</li>
 *     <li>ověřit existenci účtu a jeho aktivaci,</li>
 *     <li>namapovat {@link AppUserEntity} na {@link UserDetails} objekt,
 *         který Spring Security používá při autentizaci.</li>
 * </ul>
 *
 * JAK JE TO POUŽITÉ VE SPRING SECURITY:
 * <ul>
 *     <li>při loginu Spring Security zavolá {@link #loadUserByUsername(String)},</li>
 *     <li>vrácený {@link UserDetails} obsahuje:</li>
 *     <ul>
 *         <li>username (email),</li>
 *         <li>heslo (hashované),</li>
 *         <li>role / autority.</li>
 *     </ul>
 * </ul>
 *
 * BEZPEČNOST:
 * <ul>
 *     <li>heslo se porovnává automaticky přes {@code PasswordEncoder},</li>
 *     <li>pokud účet není aktivován ({@code enabled == false}),
 *         vyhodí se {@link AccountNotActivatedException},</li>
 *     <li>typ vyhozené výjimky přímo ovlivňuje výsledek přihlášení
 *         (např. jiná hláška pro „neaktivovaný účet“ vs. „uživatel nenalezen“).</li>
 * </ul>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Repozitář uživatelů – používá se pouze pro načtení dat pro autentizaci.
     */
    private final AppUserRepository appUserRepository;

    public CustomUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    // =====================================================
    // NAČTENÍ UŽIVATELE PRO SPRING SECURITY
    // =====================================================

    /**
     * Načte uživatele podle emailu (username) pro potřeby autentizace.
     * <p>
     * Tato metoda je volána Spring Security při přihlášení.
     *
     * @param email email zadaný uživatelem při loginu (username)
     * @return {@link UserDetails} objekt použitý pro autentizaci
     *
     * @throws UsernameNotFoundException    pokud uživatel s daným emailem neexistuje
     * @throws AccountNotActivatedException pokud účet existuje, ale není aktivní
     */
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // -------------------------------------------------
        // NAČTENÍ UŽIVATELE Z DB
        // -------------------------------------------------
        AppUserEntity user = appUserRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("BE - Uživatel nenalezen")
                );

        // -------------------------------------------------
        // KONTROLA AKTIVACE ÚČTU
        // -------------------------------------------------
        if (!user.isEnabled()) {
            // vlastní výjimka – typicky se zachytává ve filtru pro login
            // a přeloží se na vhodnou odpověď pro FE
            throw new AccountNotActivatedException();
        }

        // -------------------------------------------------
        // MAPOVÁNÍ NA UserDetails
        // -------------------------------------------------
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                /*
                 * role se uloží bez prefixu (ADMIN / MANAGER / PLAYER),
                 * Spring si automaticky přidá prefix "ROLE_".
                 * Příklad:
                 *   user.getRole() = "ROLE_ADMIN"
                 *   → .roles("ADMIN")
                 *   → výsledná autorita: "ROLE_ADMIN"
                 */
                .roles(user.getRole().name().replace("ROLE_", ""))
                /*
                 * disabled = true → účet se nemůže přihlásit.
                 * V praxi je to zde trochu redundantní (už jsme ověřili enabled),
                 * ale držíme flag konzistentní se stavem v DB.
                 */
                .disabled(!user.isEnabled())
                .build();
    }
}
