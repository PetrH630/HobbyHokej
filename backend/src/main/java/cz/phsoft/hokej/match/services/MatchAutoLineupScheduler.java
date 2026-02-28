package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.repositories.MatchRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class MatchAutoLineupScheduler {

    private final MatchRepository matchRepository;
    private final MatchAutoLineupService matchAutoLineupService;
    private final Clock clock;

    public MatchAutoLineupScheduler(
            MatchRepository matchRepository,
            MatchAutoLineupService matchAutoLineupService,
            Clock clock
    ) {
        this.matchRepository = matchRepository;
        this.matchAutoLineupService = matchAutoLineupService;
        this.clock = clock;
    }

//    @Scheduled(cron = "0 */5 * * * *") // každých 5 minut, příklad
//    public void autoLineupOneHourBefore() {
//        LocalDateTime now = LocalDateTime.now(clock);
//        LocalDateTime limitFrom = now.plusMinutes(55);
//        LocalDateTime limitTo = now.plusMinutes(65);
//
//        List<MatchEntity> upcoming = matchRepository
//                .findByDateTimeBetweenAndMatchStatusNot(
//                        limitFrom,
//                        limitTo,
//                        MatchStatus.CANCELED
//                );
//
//        for (MatchEntity match : upcoming) {
//            matchAutoLineupService.autoArrangeStartingLineup(match.getId());
//        }
//    }
}
