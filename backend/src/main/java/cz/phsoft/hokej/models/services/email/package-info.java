/**
 * Emailový subsystém aplikace.
 *
 * Zajišťuje:
 * - sestavení subjectu a těla emailových zpráv,
 * - rozhodování, zda se používá HTML nebo plain text,
 * - přípravu emailového obsahu podle typu notifikace.
 *
 * Tato vrstva:
 * - neřeší business logiku,
 * - neřeší odeslání SMS,
 * - používá se z notifikačního subsystému a service vrstev.
 */
package cz.phsoft.hokej.models.services.email;
