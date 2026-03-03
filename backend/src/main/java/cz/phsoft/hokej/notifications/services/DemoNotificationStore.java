package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.notifications.dto.DemoNotificationsDTO;
import cz.phsoft.hokej.notifications.dto.DemoNotificationsDTO.DemoEmailDTO;
import cz.phsoft.hokej.notifications.dto.DemoNotificationsDTO.DemoSmsDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Komponenta představující dočasné úložiště notifikací pro demo režim aplikace.
 *
 * Pokud je aplikace spuštěna v demo režimu, e-mailové a SMS notifikace
 * se fyzicky neodesílají, ale ukládají se do paměťové kolekce této třídy.
 * Uložené notifikace mohou být následně předány frontendové části aplikace
 * prostřednictvím příslušného DTO objektu.
 *
 * Data jsou uchovávána pouze v paměti aplikace a nejsou perzistována
 * do databáze. Třída je navržena jako Spring komponenta se sdíleným
 * životním cyklem v rámci aplikačního kontextu.
 *
 * Přístup k interním kolekcím je synchronizován z důvodu zajištění
 * vláknové bezpečnosti při souběžném použití.
 */
@Component
public class DemoNotificationStore {

    /**
     * Vnitřní kolekce pro ukládání e-mailových notifikací v demo režimu.
     */
    private final List<DemoEmailDTO> emails = new ArrayList<>();

    /**
     * Vnitřní kolekce pro ukládání SMS notifikací v demo režimu.
     */
    private final List<DemoSmsDTO> sms = new ArrayList<>();

    /**
     * Přidá e-mailovou notifikaci do demo úložiště.
     *
     * Vytvoří se nová instance {@link DemoEmailDTO} na základě předaných
     * parametrů a uloží se do interní kolekce. Operace je synchronizována
     * z důvodu ochrany proti souběžnému přístupu.
     *
     * @param to adresa příjemce e-mailu
     * @param subject předmět e-mailové zprávy
     * @param body textové nebo HTML tělo zprávy
     * @param html určuje, zda je tělo zprávy ve formátu HTML
     * @param type typ notifikace reprezentovaný výčtovým typem NotificationType
     * @param recipientKind druh příjemce notifikace, například hráč nebo administrátor
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
     * Přidá SMS notifikaci do demo úložiště.
     *
     * Vytvoří se nová instance {@link DemoSmsDTO} a uloží se do interní
     * kolekce SMS zpráv. Operace je synchronizována pro zajištění
     * konzistence dat při paralelním přístupu.
     *
     * @param to telefonní číslo příjemce
     * @param text text SMS zprávy
     * @param type typ notifikace reprezentovaný výčtovým typem NotificationType
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
     * Vrátí všechny aktuálně uložené notifikace a následně úložiště vyčistí.
     *
     * Vytvoří se nová instance {@link DemoNotificationsDTO}, která obsahuje
     * kopie interních kolekcí e-mailů a SMS zpráv. Po vytvoření DTO jsou
     * interní kolekce vymazány.
     *
     * Operace je synchronizována z důvodu zachování konzistence dat.
     *
     * @return DTO obsahující seznam uložených e-mailových a SMS notifikací
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
     * Vyčistí interní úložiště notifikací bez jejich vrácení.
     *
     * Obě kolekce e-mailů i SMS zpráv jsou vyprázdněny.
     * Operace je synchronizována pro zajištění vláknové bezpečnosti.
     */
    public synchronized void clear() {
        emails.clear();
        sms.clear();
    }
}