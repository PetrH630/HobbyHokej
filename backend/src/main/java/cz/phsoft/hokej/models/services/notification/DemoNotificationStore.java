package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.models.dto.DemoNotificationsDTO;
import cz.phsoft.hokej.models.dto.DemoNotificationsDTO.DemoEmailDTO;
import cz.phsoft.hokej.models.dto.DemoNotificationsDTO.DemoSmsDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Úložiště notifikací pro DEMO režim.
 *
 * Pokud je aplikace spuštěna v demo módu,
 * e-maily a SMS se neodesílají, ale ukládají
 * se do této třídy a následně vrací na frontend.
 *
 * Data jsou držena pouze v paměti.
 */
@Component
public class DemoNotificationStore {

    private final List<DemoEmailDTO> emails = new ArrayList<>();
    private final List<DemoSmsDTO> sms = new ArrayList<>();

    /**
     * Přidá e-mail do demo úložiště.
     */
    public synchronized void addEmail(String to,
                                      String subject,
                                      String body,
                                      boolean html,
                                      NotificationType type,
                                      String recipientKind) {

        emails.add(new DemoEmailDTO(
                to,
                subject,
                body,
                html,
                type,
                recipientKind
        ));
    }

    /**
     * Přidá SMS do demo úložiště.
     */
    public synchronized void addSms(String to,
                                    String text,
                                    NotificationType type) {

        sms.add(new DemoSmsDTO(
                to,
                text,
                type
        ));
    }

    /**
     * Vrátí všechny notifikace a následně je vymaže.
     */
    public synchronized DemoNotificationsDTO getAndClear() {

        DemoNotificationsDTO dto = new DemoNotificationsDTO(
                new ArrayList<>(emails),
                new ArrayList<>(sms)
        );

        emails.clear();
        sms.clear();

        return dto;
    }

    /**
     * Ručně vyčistí úložiště bez vrácení dat.
     */
    public synchronized void clear() {
        emails.clear();
        sms.clear();
    }
}
