package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.notifications.dto.NotificationBadgeDTO;
import cz.phsoft.hokej.notifications.dto.NotificationDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * Služba pro čtení a změnu stavu aplikačních notifikací z pohledu uživatelského rozhraní.
 *
 * Odpovědnost této služby spočívá v poskytování dotazovacích operací nad notifikacemi
 * aktuálně přihlášeného uživatele a v řízení změny jejich stavu čtení.
 * Služba pracuje vždy s uživatelem identifikovaným prostřednictvím Authentication kontextu.
 *
 * Samotné načítání dat je delegováno na perzistenční vrstvu reprezentovanou
 * NotificationRepository a transformace entit do DTO je delegována na NotificationMapper.
 */
public interface NotificationQueryService {

    /**
     * Vrací badge s počtem nepřečtených notifikací od posledního přihlášení uživatele.
     *
     * Na základě autentizačního kontextu je identifikován aktuální uživatel,
     * pro kterého je zjištěn počet nepřečtených notifikací v definovaném časovém rozsahu.
     * Výsledek je vrácen ve formě DTO určeného pro zobrazení v uživatelském rozhraní.
     *
     * @param authentication autentizační kontext aktuálně přihlášeného uživatele
     * @return DTO obsahující informace o počtu nepřečtených notifikací
     */
    NotificationBadgeDTO getBadge(Authentication authentication);

    /**
     * Vrací seznam notifikací vytvořených po posledním přihlášení uživatele.
     *
     * Pokud uživatel nemá evidovaný čas posledního přihlášení,
     * použije se výchozí časové okno definované implementací.
     * Výsledkem je seznam notifikací transformovaných do DTO,
     * které jsou určeny pro zobrazení v uživatelském rozhraní.
     *
     * @param authentication autentizační kontext aktuálně přihlášeného uživatele
     * @return seznam notifikací ve formě DTO
     */
    List<NotificationDTO> getSinceLastLogin(Authentication authentication);

    /**
     * Vrací omezený počet posledních notifikací aktuálního uživatele.
     *
     * Na základě autentizačního kontextu je identifikován uživatel,
     * pro kterého jsou načteny nejnovější notifikace.
     * Parametr limit omezuje maximální počet vrácených záznamů.
     *
     * @param authentication autentizační kontext aktuálně přihlášeného uživatele
     * @param limit maximální počet vrácených notifikací
     * @return seznam posledních notifikací ve formě DTO
     */
    List<NotificationDTO> getRecent(Authentication authentication, int limit);

    /**
     * Označí konkrétní notifikaci aktuálního uživatele jako přečtenou.
     *
     * Operace je navržena jako idempotentní. Pokud je notifikace již označena
     * jako přečtená nebo neexistuje, není vyvolána chyba.
     * Identifikace uživatele probíhá prostřednictvím Authentication kontextu.
     *
     * @param authentication autentizační kontext aktuálně přihlášeného uživatele
     * @param id identifikátor notifikace
     */
    void markAsRead(Authentication authentication, Long id);

    /**
     * Označí všechny notifikace aktuálního uživatele jako přečtené.
     *
     * Operace je aplikována na všechny existující notifikace
     * přiřazené aktuálně přihlášenému uživateli.
     * Identifikace uživatele probíhá prostřednictvím Authentication kontextu.
     *
     * @param authentication autentizační kontext aktuálně přihlášeného uživatele
     */
    void markAllAsRead(Authentication authentication);

    /**
     * Vrací omezený seznam všech notifikací v systému pro administrativní přehled.
     *
     * Tato operace není vázána na konkrétního uživatele.
     * Parametr limit omezuje maximální počet vrácených záznamů
     * z důvodu výkonu a přehlednosti.
     *
     * @param limit maximální počet vrácených notifikací
     * @return seznam notifikací ve formě DTO
     */
    List<NotificationDTO> getAllNotifications(int limit);
}