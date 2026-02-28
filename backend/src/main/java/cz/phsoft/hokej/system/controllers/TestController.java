package cz.phsoft.hokej.system.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Jednoduchý testovací REST controller.
 *
 * Slouží k ověření, že backend aplikace běží a že funguje
 * zabezpečení pro roli ADMIN.
 */
@RestController
@RequestMapping("/api/test")
@PreAuthorize("hasRole('ADMIN')")
public class TestController {

    /**
     * Vrací jednoduchou textovou zprávu pro ověření, že backend je online.
     *
     * @return textová zpráva potvrzující běh backendu
     */
    @GetMapping
    public String hello() {
        return "Backend je online!";
    }
}
