package cz.phsoft.hokej.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller, který se používá pro ladění bezpečnostního kontextu.
 *
 * Umožňuje ověřit, zda je uživatel autentizován a jaké informace
 * jsou dostupné v objektu {@link Authentication}. Controller je určen
 * pouze pro vývojové prostředí a neměl by být vystaven v produkci.
 */
@RestController
public class DebugController {

    /**
     * Vrací aktuální objekt {@link Authentication}.
     *
     * Metoda se používá výhradně pro ladění a diagnostiku
     * bezpečnostního kontextu.
     *
     * @param auth autentizační kontext aktuálního uživatele
     * @return objekt {@link Authentication} s informacemi o uživateli
     */
    @GetMapping("/api/debug/me")
    public Object me(Authentication auth) {
        return auth;
    }
}
