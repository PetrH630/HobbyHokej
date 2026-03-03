/**
 * Balíček config obsahuje konfigurační a infrastrukturní třídy aplikace.
 *
 * Třídy v tomto balíčku nastavují chování frameworku Spring
 * a tvoří technickou páteř backendu. Neobsahují doménovou
 * business logiku, ale definují způsob, jakým je aplikace
 * inicializována, zabezpečena a provozována.
 *
 * Obsah balíčku zahrnuje zejména:
 * - AppConfig pro obecnou konfiguraci aplikačních bean definic,
 * - SecurityConfig pro konfiguraci Spring Security,
 * - CustomUserDetailsService pro napojení bezpečnosti na databázi uživatelů,
 * - CustomJsonLoginFilter pro zpracování přihlašování přes JSON,
 * - GlobalExceptionHandler pro jednotné zpracování výjimek,
 * - AuditAspect pro auditní zpracování vybraných operací,
 * - DataInitializer pro inicializaci dat při startu aplikace,
 * - TimeConfig pro konfiguraci práce s časem, například definici Clock.
 *
 * Tento balíček představuje konfigurační vrstvu aplikace
 * a odděluje technické nastavení od doménových modulů.
 */
package cz.phsoft.hokej.config;