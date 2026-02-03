/**
 * Mapovací třídy pro převod mezi entitami a DTO objekty.
 *
 * Zajišťuje se:
 * - převod databázových entit na DTO pro API,
 * - převod vstupních DTO na entity pro uložení,
 * - centralizace mapovací logiky na jedno místo.
 *
 * Mapovací třídy:
 * - neobsahují business logiku,
 * - používají se typicky ve service vrstvě.
 */
package cz.phsoft.hokej.models.mappers;
