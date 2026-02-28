package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.notifications.entities.NotificationEntity;
import cz.phsoft.hokej.notifications.repositories.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationCleanupService {

    private final NotificationRepository notificationRepository;
    private final Clock clock;

    @Value("${app.notifications.retention-days:14}")
    private long retentionDays;

    @Value("${app.notifications.min-per-user:10}")
    private int minPerUser;

    public NotificationCleanupService(NotificationRepository notificationRepository,
                                      Clock clock) {
        this.notificationRepository = notificationRepository;
        this.clock = clock;
    }

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void cleanupOldNotifications() {

        Instant cutoff = Instant.now(clock)
                .minus(retentionDays, ChronoUnit.DAYS);

        List<NotificationEntity> oldNotifications =
                notificationRepository
                        .findByCreatedAtBeforeOrderByUserIdAscCreatedAtDesc(cutoff);

        if (oldNotifications.isEmpty()) {
            return;
        }

        Long currentUserId = null;
        int counterForUser = 0;

        List<NotificationEntity> toDelete = new ArrayList<>();

        for (NotificationEntity n : oldNotifications) {

            Long userId = n.getUser().getId();

            // nový uživatel
            if (!userId.equals(currentUserId)) {
                currentUserId = userId;
                counterForUser = 0;
            }

            counterForUser++;

            // pokud jsme nad limitem → označíme ke smazání
            if (counterForUser > minPerUser) {
                toDelete.add(n);
            }
        }

        if (!toDelete.isEmpty()) {
            notificationRepository.deleteAll(toDelete);
        }
    }
}