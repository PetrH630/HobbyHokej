package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.dto.MatchDTO;
import cz.phsoft.hokej.match.enums.MatchCancelReason;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;

/**
 * Service vrstva pro změnové operace nad zápasy.
 *
 * Zajišťuje vytváření, úpravu, mazání a změny stavu zápasu
 * včetně souvisejících side-effects (notifikace, přepočet kapacity,
 * úprava pozic hráčů při změně herního systému).
 */
public interface MatchCommandService {

    MatchDTO createMatch(MatchDTO dto);

    MatchDTO updateMatch(Long id, MatchDTO dto);

    SuccessResponseDTO deleteMatch(Long id);

    SuccessResponseDTO cancelMatch(Long matchId, MatchCancelReason reason);

    SuccessResponseDTO unCancelMatch(Long matchId);
}