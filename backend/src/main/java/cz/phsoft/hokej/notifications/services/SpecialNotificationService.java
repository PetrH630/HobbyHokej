package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.notifications.dto.SpecialNotificationRequestDTO;
import cz.phsoft.hokej.notifications.dto.SpecialNotificationTargetDTO;

import java.util.List;

/**
 * Služba pro odesílání speciálních zpráv administrátorem.
 *
 * Odpovědnost této služby spočívá v centralizovaném zpracování
 * administrátorských zpráv, které mají být doručeny vybraným příjemcům.
 * Služba zajišťuje vytvoření in-app notifikací a podle požadavku
 * také odeslání e-mailových a SMS zpráv.
 *
 * Speciální zprávy jsou odesílány bez ohledu na individuální
 * uživatelská notifikační nastavení, protože představují
 * administrativní nebo systémově důležitou komunikaci.
 */
public interface SpecialNotificationService {

    /**
     * Odesílá speciální zprávu na základě vstupního DTO.
     *
     * Pro každý definovaný cíl je vytvořena in-app notifikace
     * typu SPECIAL_MESSAGE. Podle nastavení ve vstupním DTO
     * může být navíc provedeno odeslání e-mailu a SMS zprávy.
     *
     * Zpracování jednotlivých příjemců je řízeno implementací služby,
     * která zajišťuje vytvoření perzistentního záznamu notifikace
     * a případné využití dalších komunikačních kanálů.
     *
     * @param request definice zprávy včetně obsahu a seznamu příjemců
     */
    void sendSpecialNotification(SpecialNotificationRequestDTO request);

    /**
     * Vrací seznam možných příjemců pro odeslání speciální zprávy.
     *
     * Do seznamu jsou zahrnuti schválení hráči, kteří mají přiřazeného
     * uživatele, a dále aktivní uživatelé bez navázaného hráče.
     * Výsledkem je seznam DTO reprezentujících cíle,
     * které mohou být vybrány administrátorem.
     *
     * @return seznam dostupných cílů pro speciální notifikaci
     */
    List<SpecialNotificationTargetDTO> getSpecialNotificationTargets();
}