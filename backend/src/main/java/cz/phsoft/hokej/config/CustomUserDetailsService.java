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
 * Implementace {@link UserDetailsService} pro napojení Spring Security
 * na databázový model uživatele.
 *
 * Třída načítá uživatele z databáze podle e-mailu, ověřuje, zda je účet
 * aktivní, a převádí entitu {@link AppUserEntity} na objekt
 * {@link UserDetails}, který Spring Security používá při autentizaci.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Repozitář pro načítání uživatelů při přihlášení.
     */
    private final AppUserRepository appUserRepository;

    public CustomUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    // Načtení uživatele pro Spring Security

    /**
     * Načte uživatele podle e-mailu pro potřeby autentizace.
     *
     * Metoda se volá Spring Security při přihlášení. V případě, že uživatel
     * neexistuje, je vyhozena {@link UsernameNotFoundException}. Pokud účet
     * existuje, ale není aktivní, je vyhozena {@link AccountNotActivatedException}.
     *
     * @param email e-mail zadaný uživatelem při přihlášení
     * @return objekt {@link UserDetails} použitý pro autentizaci
     * @throws UsernameNotFoundException    pokud uživatel s daným e-mailem neexistuje
     * @throws AccountNotActivatedException pokud účet existuje, ale není aktivní
     */
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // Načtení uživatele z databáze
        AppUserEntity user = appUserRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("BE - Uživatel nenalezen")
                );

        // Kontrola aktivace účtu
        if (!user.isEnabled()) {
            // Výjimka se typicky zachytává ve filtru pro login a převádí na odpověď pro frontend
            throw new AccountNotActivatedException();
        }

        // Mapování na UserDetails
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
