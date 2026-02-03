/**
 * Repozitářová vrstva aplikace.
 *
 * Obsahuje Spring Data JPA repozitáře pro přístup k databázi.
 *
 * Zajišťuje:
 * - načítání a ukládání entit,
 * - definici dotazovacích metod,
 * - abstrahování práce s perzistencí dat.
 *
 * Repozitáře:
 * - neobsahují business logiku,
 * - jsou používány výhradně ve service vrstvě.
 */
package cz.phsoft.hokej.data.repositories;
