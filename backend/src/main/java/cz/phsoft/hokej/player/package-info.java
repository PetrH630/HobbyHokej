/**
 * Balíček obsahující doménovou logiku týkající se hráčů.
 *
 * Obsahuje:
 * - entity reprezentující hráče a jejich nastavení,
 * - DTO objekty pro přenos dat mezi vrstvami aplikace,
 * - repository rozhraní pro přístup k perzistentní vrstvě,
 * - servisní vrstvy pro čtecí a změnové operace nad hráči,
 * - výjimky specifické pro doménu hráče.
 *
 * Architektura balíčku:
 * - oddělení čtecí a zápisové logiky pomocí Query a Command služeb,
 * - využití mapperů pro převod mezi entitami a DTO,
 * - zapouzdření doménových pravidel (například stav hráče,
 *   období neaktivity nebo statistiky hráče).
 *
 * Balíček neobsahuje HTTP vrstvu ani bezpečnostní konfiguraci.
 * Tyto oblasti jsou řešeny v nadřazených vrstvách aplikace.
 */
package cz.phsoft.hokej.player;