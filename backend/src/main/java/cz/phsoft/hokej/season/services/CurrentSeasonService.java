package cz.phsoft.hokej.season.services;

/**
 * Rozhraní se používá pro správu aktuálně zvolené sezóny
 * v kontextu přihlášeného uživatele.
 *
 * Sezóna slouží jako globální kontextový filtr pro většinu
 * aplikačních operací, jako jsou zápasy, statistiky a přehledy.
 * Uživatel může pracovat buď s automaticky zvolenou aktivní sezónou,
 * nebo si může sezónu explicitně vybrat.
 *
 * Rozhraní definuje jednotný kontrakt pro práci s aktuální sezónou
 * a odděluje práci se session od business logiky správy sezón.
 */
public interface CurrentSeasonService {

    /**
     * Vrátí identifikátor aktuálně používané sezóny.
     *
     * Pokud je v session uložena sezóna, kterou si uživatel
     * explicitně zvolil, vrátí se tato hodnota.
     * Pokud sezóna nastavena není nebo nebyla zvolena vědomě,
     * použije se globálně aktivní sezóna definovaná v systému.
     *
     * Při použití globálně aktivní sezóny se její identifikátor
     * uloží do session jako automaticky zvolený.
     *
     * @return ID aktuální sezóny nebo null, pokud neexistuje žádná aktivní sezóna
     */
    Long getCurrentSeasonIdOrDefault();

    /**
     * Nastaví sezónu jako aktuální pro přihlášeného uživatele.
     *
     * Metoda se používá v okamžiku, kdy si uživatel sezónu
     * explicitně vybere. Zvolená sezóna je uložena do session
     * jako uživatelská volba.
     *
     * @param seasonId ID sezóny, která má být nastavena jako aktuální
     */
    void setCurrentSeasonId(Long seasonId);

    /**
     * Odstraní informaci o aktuální sezóně z uživatelského kontextu.
     *
     * Metoda se používá zejména při odhlášení uživatele
     * nebo při resetu session.
     */
    void clearCurrentSeason();
}
