/**
 * Balíček obsahující modul notifikací aplikace.
 *
 * Tento balíček představuje samostatnou funkční oblast systému,
 * která zajišťuje vytváření, ukládání a doručování notifikací
 * uživatelům aplikace.
 *
 * Odpovědnosti modulu zahrnují zejména:
 * - ukládání in-app notifikací do databáze,
 * - poskytování notifikací pro uživatelské rozhraní,
 * - správu stavu přečtení notifikací,
 * - odesílání e-mailových a SMS notifikací,
 * - podporu speciálních administrátorských zpráv,
 * - integraci s DEMO režimem aplikace.
 *
 * Modul je navržen v souladu s principy oddělení odpovědností.
 * Řadiče delegují aplikační logiku do service vrstvy,
 * která komunikuje s repository vrstvou a externími komunikačními službami.
 *
 * Balíček spolupracuje zejména s moduly user, player,
 * match a demo.
 */
package cz.phsoft.hokej.notifications;