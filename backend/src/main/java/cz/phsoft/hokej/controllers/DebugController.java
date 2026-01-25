package cz.phsoft.hokej.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Debug controller pro účely vývoje a ladění bezpečnostního kontextu.
 * <p>
 * Slouží k ověření:
 * <ul>
 *     <li>zda je uživatel autentizován,</li>
 *     <li>jaké údaje jsou dostupné v {@link Authentication},</li>
 *     <li>jaké role a authority má aktuální uživatel.</li>
 * </ul>
 *
 * Tento controller by měl být používán pouze ve vývojovém prostředí
 * a neměl by být dostupný v produkci.
 */
@RestController
public class DebugController {

    /**
     * Vrátí aktuální {@link Authentication} objekt.
     * <p>
     * Endpoint slouží výhradně pro ladění a diagnostiku.
     *
     * @param auth autentizační kontext aktuálního uživatele
     * @return objekt {@link Authentication}
     */
    @GetMapping("/api/debug/me")
    public Object me(Authentication auth) {
        return auth;
    }
}
