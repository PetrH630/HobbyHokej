package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.dto.MatchDTO;
import cz.phsoft.hokej.match.enums.MatchCancelReason;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;

/**
 * Service vrstva pro změnové operace nad zápasy.
 *
 * Zajišťuje vytváření, úpravu, mazání a změny stavu zápasu
 * včetně souvisejících side-effects (notifikace, přepočet kapacity,
 * úprava pozic hráčů při změně herního systému).
 */
public interface MatchCommandService {

    /**
     * Vytváří nový zápas podle dodaného DTO.
     *
     * Zápas se ukládá do databáze, nastavuje se příslušná sezóna
     * a identifikace uživatele, který zápas vytvořil. Před uložením
     * se kontroluje, zda datum zápasu spadá do aktivní sezóny.
     *
     * @param dto DTO obsahující údaje o zápasu, který se vytváří.
     * @return DTO reprezentující nově vytvořený zápas po uložení.
     */
    MatchDTO createMatch(MatchDTO dto);

    /**
     * Aktualizuje existující zápas podle dodaného DTO.
     *
     * Při aktualizaci se provádí kontrola oprávnění podle role
     * přihlášeného uživatele a kontrola, zda zápas náleží do
     * aktivní sezóny, pokud úpravu neprovádí administrátor nebo
     * manažer. Při změně klíčových parametrů se může změnit stav
     * zápasu a mohou se vyvolat navazující procesy (přepočet
     * kapacity, změna rozložení hráčů, notifikace).
     *
     * @param id  Identifikátor zápasu, který se má upravit.
     * @param dto DTO s novými údaji pro daný zápas.
     * @return DTO reprezentující upravený zápas po uložení.
     */
    MatchDTO updateMatch(Long id, MatchDTO dto);

    /**
     * Maže existující zápas.
     *
     * Při mazání se kontroluje, zda aplikace neběží v DEMO režimu.
     * V DEMO režimu se fyzické odstranění nepovoluje a operace
     * se ukončí výjimkou. V opačném případě se zápas odstraní
     * z databáze a vrátí se potvrzující odpověď.
     *
     * @param id Identifikátor zápasu, který se má smazat.
     * @return Odpověď s informací o úspěšném odstranění zápasu.
     */
    SuccessResponseDTO deleteMatch(Long id);

    /**
     * Ruší plánovaný zápas zvoleným důvodem.
     *
     * Zápasu se nastaví stav CANCELED a příslušný důvod zrušení.
     * Při zrušení se hráčům s relevantní registrací (REGISTERED,
     * RESERVED, SUBSTITUTE) odešlou notifikace o zrušení zápasu.
     * Pokud je zápas již zrušen, operace se ukončí výjimkou.
     *
     * @param matchId Identifikátor zápasu, který se má zrušit.
     * @param reason  Důvod, proč je zápas rušen.
     * @return Odpověď s informací o úspěšném zrušení zápasu.
     */
    SuccessResponseDTO cancelMatch(Long matchId, MatchCancelReason reason);

    /**
     * Obnovuje dříve zrušený zápas.
     *
     * Zápasu se změní stav ze CANCELED na UNCANCELED a odstraní se
     * důvod zrušení. Následně se hráčům s relevantní registrací
     * odešlou notifikace o obnovení zápasu. Pokud zápas není ve stavu
     * CANCELED, operace se ukončí výjimkou.
     *
     * @param matchId Identifikátor zápasu, který se má obnovit.
     * @return Odpověď s informací o úspěšném obnovení zápasu.
     */
    SuccessResponseDTO unCancelMatch(Long matchId);

    /**
     * Aktualizuje skóre zápasu.
     *
     * Metoda nastaví počty branek pro tým LIGHT a tým DARK. Skóre se
     * povoluje měnit pouze u nezrušených zápasů. Při změně skóre se
     * nastaví stav zápasu na UPDATED, aby bylo možné odlišit, že došlo
     * k úpravě výsledku.
     *
     * @param matchId    Identifikátor zápasu, jehož skóre se upravuje.
     * @param scoreLight Počet branek týmu LIGHT. Hodnota musí být nezáporná.
     * @param scoreDark  Počet branek týmu DARK. Hodnota musí být nezáporná.
     * @return DTO reprezentující zápas po uložení nového skóre.
     */
    MatchDTO updateMatchScore(Long matchId, Integer scoreLight, Integer scoreDark);
}