package cz.phsoft.hokej.config;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.user.repositories.AppUserRepository;
import cz.phsoft.hokej.user.exceptions.AccountNotActivatedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementace UserDetailsService pro napojení Spring Security
 * na databázový model uživatele.
 *
 * Třída načítá uživatele z databáze podle e-mailu, ověřuje, zda je účet
 * aktivní, a převádí entitu AppUserEntity na objekt UserDetails,
 * který Spring Security používá při autentizaci.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Repozitář pro načítání uživatelů při přihlášení.
     *
     * Repozitář zajišťuje přístup k entitám AppUserEntity v databázi.
     */
    private final AppUserRepository appUserRepository;

    /**
     * Vytváří službu pro načítání uživatelských detailů.
     *
     * @param appUserRepository repozitář pro práci s entitou AppUserEntity
     */
    public CustomUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    // Načtení uživatele pro Spring Security

    /**
     * Načte uživatele podle e-mailu pro potřeby autentizace.
     *
     * Metoda se volá Spring Security při přihlášení. V případě, že uživatel
     * neexistuje, je vyhozena UsernameNotFoundException. Pokud účet existuje,
     * ale není aktivní, je vyhozena AccountNotActivatedException.
     *
     * @param email e-mail zadaný uživatelem při přihlášení
     * @return objekt UserDetails použitý pro autentizaci
     * @throws UsernameNotFoundException    pokud uživatel s daným e-mailem neexistuje
     * @throws AccountNotActivatedException pokud účet existuje, ale není aktivní
     */
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        AppUserEntity user = appUserRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("BE - Uživatel nenalezen")
                );

        if (!user.isEnabled()) {
            // Výjimka se typicky zachytává ve filtru pro login a převádí na odpověď pro frontend
            throw new AccountNotActivatedException();
        }

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                // Role se ukládá bez prefixu ROLE_, Spring si prefix přidá automaticky
                .roles(user.getRole().name().replace("ROLE_", ""))
                // Disabled flag se drží konzistentní se stavem entity
                .disabled(!user.isEnabled())
                .build();
    }
}