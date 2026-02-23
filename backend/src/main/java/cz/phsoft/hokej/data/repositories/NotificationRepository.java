package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repozitář pro práci s notifikacemi.
 *
 * Poskytuje metody pro načítání notifikací dle uživatele,
 * filtrování podle času vytvoření a příznaku přečtení
 * a pro mazání starších notifikací.
 *
 * Metody se používají zejména pro:
 * - výpis notifikací od posledního přihlášení,
 * - výpočet badge (nepřečtené notifikace),
 * - přehled posledních událostí,
 * - pravidelné mazání starých záznamů.
 */
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    /**
     * Vyhledává notifikace daného uživatele vytvořené po zadaném čase.
     *
     * Výsledek je seřazen podle času vytvoření od nejnovějších
     * k nejstarším. Metoda se používá pro výpis notifikací
     * od posledního přihlášení.
     *
     * @param user        uživatel, pro kterého se notifikace hledají
     * @param createdAfter časová hranice (notifikace vytvořené po tomto čase)
     * @return seznam notifikací
     */
    List<NotificationEntity> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(
            AppUserEntity user,
            Instant createdAfter
    );

    /**
     * Počítá nepřečtené notifikace daného uživatele vytvořené
     * po zadaném čase.
     *
     * Metoda se používá pro zobrazení počtu nových notifikací (badge)
     * od posledního přihlášení.
     *
     * @param user        uživatel, pro kterého se notifikace počítají
     * @param createdAfter časová hranice (notifikace vytvořené po tomto čase)
     * @return počet nepřečtených notifikací
     */
    long countByUserAndCreatedAtAfterAndReadAtIsNull(
            AppUserEntity user,
            Instant createdAfter
    );

    /**
     * Vyhledává poslední notifikace daného uživatele bez ohledu
     * na stav přečtení.
     *
     * Metoda se používá pro přehled posledních událostí
     * (např. na dashboardu). Počet výsledků je omezen
     * klauzulí TOP v názvu metody.
     *
     * @param user uživatel, pro kterého se notifikace hledají
     * @return seznam nejnovějších notifikací
     */
    List<NotificationEntity> findTop50ByUserOrderByCreatedAtDesc(AppUserEntity user);

    /**
     * Vyhledává poslední notifikace daného uživatele vytvořené
     * po zadaném čase.
     *
     * Metoda se používá pro přehled událostí v daném časovém okně,
     * typicky za poslední dny.
     *
     * @param user         uživatel, pro kterého se notifikace hledají
     * @param createdAfter časová hranice (notifikace vytvořené po tomto čase)
     * @return seznam nejnovějších notifikací
     */
    List<NotificationEntity> findTop50ByUserAndCreatedAtAfterOrderByCreatedAtDesc(
            AppUserEntity user,
            Instant createdAfter
    );

    /**
     * Vyhledává všechny nepřečtené notifikace daného uživatele.
     *
     * Metoda se používá například při označení všech notifikací
     * jako přečtených.
     *
     * @param user uživatel, pro kterého se notifikace hledají
     * @return seznam nepřečtených notifikací
     */
    List<NotificationEntity> findByUserAndReadAtIsNullOrderByCreatedAtDesc(AppUserEntity user);

    /**
     * Maže všechny notifikace vytvořené před zadaným časem.
     *
     * Metoda se používá v plánovaném úklidu, typicky pro odstranění
     * notifikací starších než 14 dní.
     *
     * @param threshold časová hranice (notifikace vytvořené před tímto časem budou smazány)
     */
    void deleteByCreatedAtBefore(Instant threshold);

    /**
     * Vyhledává notifikaci podle identifikátoru a uživatele.
     *
     * Metoda se používá pro bezpečné načtení notifikace při označení
     * jako přečtené, aby uživatel nemohl manipulovat s cizími
     * notifikacemi.
     *
     * @param id   identifikátor notifikace
     * @param user uživatel, který notifikaci vlastní
     * @return notifikace, pokud existuje a patří danému uživateli
     */
    Optional<NotificationEntity> findByIdAndUser(Long id, AppUserEntity user);

    /**
     * Vyhledává všechny notifikace vytvořené před zadaným časem.
     *
     * Používá se v plánovaném úklidu pro získání
     * starých notifikací, které se následně filtrují
     * podle uživatele.
     *
     * @param threshold časová hranice
     * @return seznam notifikací starších než threshold
     */
    List<NotificationEntity> findByCreatedAtBeforeOrderByUserIdAscCreatedAtDesc(
            Instant threshold
    );

    /**
     * Vyhledává všechny notifikace v systému seřazené od nejnovějších.
     *
     * Metoda se používá zejména v administrátorském přehledu
     * všech notifikací bez omezení na konkrétního uživatele.
     *
     * @return seznam všech notifikací seřazených podle času vytvoření
     */
    List<NotificationEntity> findAllByOrderByCreatedAtDesc();
}