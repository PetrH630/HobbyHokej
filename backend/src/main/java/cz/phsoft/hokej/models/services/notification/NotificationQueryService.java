package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.models.dto.NotificationBadgeDTO;
import cz.phsoft.hokej.models.dto.NotificationDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * Služba pro práci s aplikačními notifikacemi z pohledu UI.
 *
 * Poskytuje metody pro:
 * - výpočet badge s počtem nepřečtených notifikací,
 * - načtení notifikací od posledního přihlášení,
 * - načtení posledních notifikací,
 * - označení notifikací jako přečtených.
 *
 * Veškerá logika čtení a změny stavu notifikací
 * se deleguje do NotificationRepository a NotificationMapper.
 * Služba pracuje vždy s aktuálně přihlášeným uživatelem
 * na základě Authentication kontextu.
 */
public interface NotificationQueryService {

    /**
     * Vrací badge s počtem nepřečtených notifikací od posledního přihlášení.
     *
     * @param authentication autentizační kontext aktuálního uživatele
     * @return DTO s informacemi o badge
     */
    NotificationBadgeDTO getBadge(Authentication authentication);

    /**
     * Vrací seznam notifikací vytvořených po posledním přihlášení uživatele.
     *
     * Pokud uživatel nemá lastLoginAt, použije se výchozí časové okno.
     *
     * @param authentication autentizační kontext aktuálního uživatele
     * @return seznam notifikací ve formě DTO
     */
    List<NotificationDTO> getSinceLastLogin(Authentication authentication);

    /**
     * Vrací poslední notifikace aktuálního uživatele.
     *
     * @param authentication autentizační kontext aktuálního uživatele
     * @param limit maximální počet vrácených záznamů
     * @return seznam notifikací ve formě DTO
     */
    List<NotificationDTO> getRecent(Authentication authentication, int limit);

    /**
     * Označí konkrétní notifikaci aktuálního uživatele jako přečtenou.
     *
     * Operace je idempotentní – pokud je notifikace již přečtená
     * nebo neexistuje, nevyvolá se chyba.
     *
     * @param authentication autentizační kontext aktuálního uživatele
     * @param id identifikátor notifikace
     */
    void markAsRead(Authentication authentication, Long id);

    /**
     * Označí všechny notifikace aktuálního uživatele jako přečtené.
     *
     * @param authentication autentizační kontext aktuálního uživatele
     */
    void markAllAsRead(Authentication authentication);

    /**
     * Vrací všechny notifikace v systému pro administrativní přehled.
     *
     * Parametr limit omezuje maximální počet vrácených záznamů
     * kvůli výkonu a přehlednosti.
     *
     * @param limit maximální počet vrácených záznamů
     * @return seznam notifikací ve formě DTO
     */
    List<NotificationDTO> getAllNotifications(int limit);
}