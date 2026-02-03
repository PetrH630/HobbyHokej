/**
 * Databázové entity aplikace.
 *
 * Reprezentují perzistentní doménový model systému
 * mapovaný pomocí JPA/Hibernate na databázové tabulky.
 *
 * Entity:
 * - definují vztahy mezi daty (OneToMany, ManyToOne, apod.),
 * - nejsou určeny pro přímou komunikaci s frontendem,
 * - jsou používány výhradně v repository a service vrstvě.
 */
package cz.phsoft.hokej.data.entities;
