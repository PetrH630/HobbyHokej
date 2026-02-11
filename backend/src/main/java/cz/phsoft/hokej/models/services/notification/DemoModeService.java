package cz.phsoft.hokej.models.services.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Služba, která poskytuje informaci, zda je aplikace v demo režimu.
 *
 * Hodnota se čte z application properties:
 *   hobbyhokej.demo-mode=true/false
 *
 * Pokud není vlastnost nastavena, výchozí hodnota je false.
 */
@Component
public class DemoModeService {

    private final boolean demoMode;

    public DemoModeService(
            @Value("${app.demo-mode:false}") boolean demoMode
    ) {
        this.demoMode = demoMode;
    }

    /**
     * Vrací true, pokud je aplikace spuštěna v demo režimu.
     */
    public boolean isDemoMode() {
        return demoMode;
    }
}
