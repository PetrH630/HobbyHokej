package cz.phsoft.hokej.system.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller určený pro ladění bezpečnostního kontextu.
 *
 * Slouží k ověření, zda je uživatel autentizován
 * a jaké informace jsou dostupné v objektu Authentication.
 * Controller je určen pouze pro vývojové účely.
 */
@RestController
public class DebugController {

    /**
     * Vrací aktuální objekt Authentication.
     *
     * Metoda se používá výhradně pro ladění
     * a diagnostiku bezpečnostního kontextu.
     *
     * @param auth autentizační kontext aktuálního uživatele
     * @return objekt Authentication s informacemi o uživateli
     */
    @GetMapping("/api/debug/me")
    public Object me(Authentication auth) {
        return auth;
    }
}