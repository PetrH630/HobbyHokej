package cz.phsoft.hokej.player.repositories;

import cz.phsoft.hokej.player.entities.PlayerInactivityPeriodEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repozitář pro práci s entitou PlayerInactivityPeriodEntity.
 *
 * Slouží k evidenci a vyhledávání období neaktivity hráčů,
 * zejména pro ověření dostupnosti hráče v konkrétním čase
 * a pro kontrolu překryvů jednotlivých období.
 */
@Repository
public interface PlayerInactivityPeriodRepository
        extends JpaRepository<PlayerInactivityPeriodEntity, Long> {

    /**
     * Ověří, zda je hráč v daném časovém intervalu neaktivní.
     *
     * Používá se například při registraci hráče na zápas
     * nebo při kontrole jeho dostupnosti.
     *
     * @param player hráč, jehož neaktivita se ověřuje
     * @param from   začátek kontrolovaného intervalu
     * @param to     konec kontrolovaného intervalu
     * @return true, pokud alespoň jedno období neaktivity zasahuje do intervalu
     */
    boolean existsByPlayerAndInactiveFromLessThanEqualAndInactiveToGreaterThanEqual(
            PlayerEntity player,
            LocalDateTime from,
            LocalDateTime to
    );

    /**
     * Vrací všechna období neaktivity hráče, která
     * se překrývají se zadaným časovým intervalem.
     *
     * Používá se zejména při validaci vytváření
     * nebo úpravy období neaktivity.
     *
     * @param player hráč
     * @param from   začátek kontrolovaného intervalu
     * @param to     konec kontrolovaného intervalu
     * @return seznam překrývajících se období neaktivity
     */
    List<PlayerInactivityPeriodEntity>
    findByPlayerAndInactiveToGreaterThanEqualAndInactiveFromLessThanEqual(
            PlayerEntity player,
            LocalDateTime from,
            LocalDateTime to
    );

    /**
     * Vrací všechna období neaktivity daného hráče
     * seřazená vzestupně podle začátku neaktivity.
     *
     * @param player hráč
     * @return seznam období neaktivity hráče seřazený podle inactiveFrom
     */
    List<PlayerInactivityPeriodEntity>
    findByPlayerOrderByInactiveFromAsc(PlayerEntity player);
}