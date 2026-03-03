package cz.phsoft.hokej.season.exceptions;

import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Výjimka signalizující, že požadovaná sezóna nebyla nalezena.
 *
 * Používá se v servisní vrstvě při hledání sezóny podle ID
 * nebo v situaci, kdy není nastavena žádná aktivní sezóna.
 *
 * Výjimka reprezentuje situaci, kdy požadovaný zdroj
 * neexistuje, a je typicky mapována na HTTP status NOT_FOUND.
 */
public class SeasonNotFoundException extends BusinessException {

    /**
     * Vytvoří výjimku pro případ, kdy sezóna s konkrétním ID
     * nebyla nalezena.
     *
     * @param id ID sezóny
     */
    public SeasonNotFoundException(Long id) {
        super("BE - Sezóna s ID " + id + " nebyla nalezena.", HttpStatus.NOT_FOUND);
    }

    /**
     * Vytvoří výjimku s vlastní chybovou zprávou.
     *
     * Používá se například v situaci, kdy není nastavena
     * žádná aktivní sezóna.
     *
     * @param message chybová zpráva
     */
    public SeasonNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}