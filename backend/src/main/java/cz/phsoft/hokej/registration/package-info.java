/**
 * Balíček registration obsahuje veškerou aplikační logiku
 * související s registracemi hráčů na zápasy.
 *
 * Zodpovídá za:
 * - evidenci registrací hráčů k jednotlivým zápasům,
 * - správu stavů registrací (REGISTERED, RESERVED, SUBSTITUTE, EXCUSED, NO_EXCUSED, UNREGISTERED),
 * - pravidla přidělování míst podle kapacity zápasu a pozic,
 * - práci s historií změn registrací pro auditní účely.
 *
 * Balíček typicky obsahuje:
 * - entity reprezentující registrace a jejich historii,
 * - repository rozhraní pro přístup k datům,
 * - service vrstvy pro příkazové a čtecí operace,
 * - DTO objekty pro přenos dat do controller vrstvy,
 * - mappovací komponenty pro převod entit na DTO,
 * - výjimky a pomocné utility související s registracemi.
 *
 * Logika v tomto balíčku úzce spolupracuje s balíčky match,
 * player, notifications a season, ale zachovává vlastní
 * odpovědnost za doménu registrací.
 */
package cz.phsoft.hokej.registration;