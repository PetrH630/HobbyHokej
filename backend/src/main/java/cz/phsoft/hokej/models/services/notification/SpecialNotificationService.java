package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.models.dto.requests.SpecialNotificationRequestDTO;
import cz.phsoft.hokej.models.dto.SpecialNotificationTargetDTO;
import java.util.List;

/**
 * Služba pro odesílání speciálních zpráv administrátorem.
 *
 * Zajišťuje vytvoření in-app notifikací a podle nastavení
 * také odeslání emailů a SMS zpráv, bez ohledu na
 * individuální uživatelská notifikační nastavení.
 */
public interface SpecialNotificationService {

    /**
     * Odesílá speciální zprávu na základě vstupního DTO.
     *
     * Pro každý uvedený cíl se:
     * - uloží in-app notifikace typu SPECIAL_MESSAGE,
     * - volitelně odešle email,
     * - volitelně odešle SMS.
     *
     * @param request definice zprávy a seznam příjemců
     */
    void sendSpecialNotification(SpecialNotificationRequestDTO request);

    /**
     * Načítá seznam možných příjemců speciální zprávy.
     *
     * Zahrnuje:
     * - schválené hráče (approved), kteří mají přiřazeného uživatele,
     * - aktivní uživatele bez hráče (enabled).
     *
     * @return seznam cílů pro speciální notifikaci
     */
    List<SpecialNotificationTargetDTO> getSpecialNotificationTargets();
}

