package cz.phsoft.hokej.season.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující pokus o vytvoření nebo aktualizaci sezóny
 * s duplicitním názvem.
 *
 * Používá se v servisní vrstvě při porušení pravidla jedinečnosti
 * názvu sezóny. Výjimka je následně zpracována globálním
 * exception handlerem a vrací HTTP status CONFLICT.
 *
 * Dědí z BusinessException, která zapouzdřuje aplikační chyby
 * reprezentující porušení doménových pravidel.
 */
public class DuplicateSeasonNameException extends BusinessException {

    /**
     * Vytvoří výjimku s detailní zprávou obsahující název sezóny,
     * který způsobil kolizi.
     *
     * @param seasonName název sezóny, který již v systému existuje
     */
    public DuplicateSeasonNameException(String seasonName) {
        super(
                "Sezóna s názvem: " + seasonName + " již existuje",
                HttpStatus.CONFLICT
        );
    }
}
