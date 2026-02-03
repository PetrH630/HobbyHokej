/**
 * Datové přenosové objekty (DTO).
 *
 * Slouží k přenosu dat mezi:
 * - controllerovou a service vrstvou,
 * - backendem a frontendem aplikace.
 *
 * DTO objekty:
 * - neobsahují business logiku,
 * - mohou obsahovat validační anotace,
 * - reprezentují data ve formátu vhodném pro API.
 *
 * Mapování mezi entitami a DTO je řešeno pomocí mapperů.
 */
package cz.phsoft.hokej.models.dto;
