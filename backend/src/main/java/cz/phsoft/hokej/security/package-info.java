/**
 * Balíček security obsahuje bezpečnostní a kontextové komponenty aplikace.
 *
 * Součástí balíčku jsou:
 * - pomocné třídy pro práci s bezpečnostním kontextem,
 * - filtry navázané na životní cyklus HTTP requestu,
 * - ThreadLocal kontexty používané během zpracování požadavku,
 * - bezpečnostní helpery určené pro použití v PreAuthorize výrazech,
 * - konstanty pro práci s HTTP session.
 *
 * Třídy v tomto balíčku doplňují standardní konfiguraci Spring Security
 * o aplikačně specifickou logiku, například práci s aktuálně zvoleným
 * hráčem nebo kontrolu vlastnictví entit.
 *
 * Primární autentizace a autorizace je řešena konfigurací Spring Security,
 * zatímco tento balíček poskytuje nadstavbové mechanismy
 * pro jemnozrnnou kontrolu přístupu a kontext requestu.
 */
package cz.phsoft.hokej.security;