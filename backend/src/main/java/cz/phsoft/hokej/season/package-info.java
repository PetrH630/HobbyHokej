/**
 * Balíček obsahující kompletní doménovou logiku pro správu sezón v aplikaci.
 *
 * Sezóna představuje časové období, ve kterém se konají zápasy
 * a ke kterému se vztahují statistiky, přehledy a další aplikační operace.
 *
 * Balíček zahrnuje:
 * - entity reprezentující perzistentní model sezóny a její historie,
 * - DTO objekty používané pro přenos dat mezi backendem a klientem,
 * - mapovací vrstvy oddělující entitní model od prezentační vrstvy,
 * - repository rozhraní pro přístup k datům,
 * - service vrstvy obsahující business logiku a správu aktivní sezóny,
 * - controller vrstvu poskytující REST API pro administrativní i uživatelskou práci se sezónami,
 * - doménové výjimky reprezentující porušení pravidel konzistence sezón.
 *
 * Architektonicky je balíček navržen podle principů vrstvené architektury:
 * - controller vrstva deleguje požadavky do service vrstvy,
 * - service vrstva obsahuje validační a stavovou logiku,
 * - repository vrstva zajišťuje komunikaci s databází,
 * - mapovací vrstva odděluje entitní model od API kontraktu.
 *
 * Balíček je klíčovým kontextovým prvkem aplikace, protože aktivní sezóna
 * ovlivňuje chování dalších modulů, například správu zápasů a statistik.
 */
package cz.phsoft.hokej.season;