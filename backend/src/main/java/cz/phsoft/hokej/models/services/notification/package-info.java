/**
 * Notifikační subsystém aplikace.
 *
 * Zajišťuje rozhodování o odesílání notifikací
 * a přípravu kontextu pro emailové a SMS zprávy.
 *
 * Obsahuje:
 * - rozhodovací logiku podle typu události,
 * - kontextové objekty pro generování zpráv,
 * - vazby na emailové a SMS služby.
 *
 * Neřeší samotné odesílání zpráv.
 */
package cz.phsoft.hokej.models.services.notification;
