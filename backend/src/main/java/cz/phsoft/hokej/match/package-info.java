/**
 * Balíček obsahující doménovou logiku související se zápasy.
 *
 * Tento balíček představuje modul odpovědný za správu zápasů
 * v rámci aplikace HobbyHokej. Obsahuje entity, DTO objekty,
 * služby, repository, mapovací komponenty a případné utilitní
 * třídy související se životním cyklem zápasu.
 *
 * Odpovědnosti modulu zahrnují zejména:
 * - vytváření a správu zápasů,
 * - změny stavu zápasu,
 * - evidenci výsledků a režimu zápasu,
 * - práci se soupiskami a rozestavením hráčů,
 * - poskytování dat pro přehledy nadcházejících a minulých zápasů.
 *
 * Modul je navržen v souladu s principy oddělení odpovědností.
 * Řadiče delegují aplikační logiku do service vrstvy,
 * která komunikuje s perzistentní vrstvou prostřednictvím repository.
 *
 * Balíček tvoří samostatnou funkční oblast systému a komunikuje
 * s dalšími moduly, zejména s moduly player, registration
 * a notifications.
 */
package cz.phsoft.hokej.match;