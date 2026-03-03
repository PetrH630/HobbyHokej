/**
 * Balíček system.controllers obsahuje infrastrukturní a pomocné
 * REST controllery aplikace.
 *
 * Třídy v tomto balíčku neposkytují doménovou logiku konkrétních
 * částí systému, ale zajišťují podpůrné funkcionality, například:
 * - poskytování informací o režimu aplikace,
 * - autentizační a registrační endpointy,
 * - testovací a ladicí endpointy,
 * - ověřování dostupnosti backendu,
 * - testování integračních služeb, například e-mailu.
 *
 * Business logika je delegována do servisních vrstev.
 * Tento balíček slouží primárně jako vstupní HTTP vrstva
 * pro systémové a technické operace.
 */
package cz.phsoft.hokej.system.controllers;