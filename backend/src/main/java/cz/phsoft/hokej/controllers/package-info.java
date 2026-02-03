/**
 * Controllerová vrstva aplikace.
 *
 * Obsahuje REST kontrolery odpovědné za:
 * - příjem HTTP požadavků z frontend aplikace,
 * - validaci vstupních dat,
 * - mapování requestů na service vrstvu,
 * - návrat standardizovaných API odpovědí.
 *
 * Tato vrstva:
 * - neobsahuje business logiku,
 * - nepracuje přímo s databází,
 * - deleguje veškerou aplikační logiku na service třídy.
 */

package cz.phsoft.hokej.controllers;