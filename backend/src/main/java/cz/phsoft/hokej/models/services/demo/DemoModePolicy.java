package cz.phsoft.hokej.models.services.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Politika pro vyhodnocování pravidel demo režimu.
 *
 * Třída se používá pro centralizaci rozhodování, zda je aplikace spuštěna
 * v demo režimu a zda je konkrétní uživatel považován za chráněného
 * demo uživatele. Tato politika je využívána zejména třídou DemoModeGuard
 * ve service vrstvě při blokování write operací.
 *
 * Aktivace demo režimu se řídí konfigurační vlastností aplikace.
 */
@Component
public class DemoModePolicy {

    /**
     * Příznak určující, zda je aplikace spuštěna v demo režimu.
     *
     * Hodnota se načítá z konfigurační vlastnosti app.demo-mode.
     */
    @Value("${app.demo-mode:false}")
    private boolean demoMode;

    /**
     * Vrací informaci, zda je aplikace spuštěna v demo režimu.
     *
     * Metoda se používá zejména pro podmíněné chování ve service vrstvě.
     *
     * @return True, pokud je demo režim aktivní, jinak false.
     */
    public boolean isDemoMode() {
        return demoMode;
    }

    /**
     * Vyhodnocuje, zda je daný uživatel považován za chráněného demo uživatele.
     *
     * Uživatel je považován za chráněného, pokud je aplikace spuštěna
     * v demo režimu a jeho identifikátor spadá do definovaného rozsahu.
     * Tato metoda se používá pro blokování write operací nad demonstračními daty.
     *
     * @param userId Identifikátor uživatele, který má být vyhodnocen.
     * @return True, pokud je uživatel chráněný v demo režimu, jinak false.
     */
    public boolean isProtectedDemoUser(Long userId) {
        return demoMode && userId != null && userId <= 10;
    }
}
