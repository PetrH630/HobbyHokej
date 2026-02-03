/**
 * Service vrstva aplikace.
 *
 * Obsahuje aplikační a business logiku systému.
 *
 * Zajišťuje:
 * - zpracování požadavků z controllerů,
 * - koordinaci práce mezi repozitáři,
 * - rozhodovací logiku a validace doménových pravidel,
 * - vyvolávání notifikací a dalších vedlejších procesů.
 *
 * Service vrstva:
 * - neřeší HTTP ani prezentaci,
 * - používá repository pro přístup k datům,
 * - pracuje s DTO objekty.
 */
package cz.phsoft.hokej.models.services;
