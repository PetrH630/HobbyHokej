package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.notifications.entities.NotificationEntity;
import cz.phsoft.hokej.notifications.repositories.NotificationRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Služba pro pravidelný úklid starých aplikačních notifikací.
 *
 * Odpovědnost třídy:
 * - identifikovat notifikace starší než definovaný počet dní,
 * - mazat staré notifikace při zachování minimálního počtu záznamů
 *   na uživatele,
 * - spouštět úklid na základě naplánovaného CRON úkolu.
 *
 * Třída neřeší:
 * - vytváření notifikací,
 * - logiku zobrazení notifikací v uživatelském rozhraní.
 */
@Service
public class NotificationCleanupService {

    /**
     * Repository pro práci s entitami NotificationEntity.
     *
     * Používá se pro vyhledání starých notifikací a jejich hromadné mazání.
     */
    private final NotificationRepository notificationRepository;

    /**
     * Hodiny poskytující aktuální čas.
     *
     * Umožňují deterministické testování a přesné určení časového řezu
     * pro odstraňování notifikací.
     */
    private final Clock clock;

    /**
     * Počet dní, po kterých se považují notifikace za staré.
     *
     * Hodnota se načítá z konfigurace aplikace. Výchozí hodnota je 14 dní.
     */
    @Value("${app.notifications.retention-days:14}")
    private long retentionDays;

    /**
     * Minimální počet notifikací, které se ponechají v databázi pro každého uživatele.
     *
     * I starší notifikace se ponechají, pokud by jejich smazání způsobilo,
     * že by uživatel měl méně než tento minimální počet záznamů.
     */
    @Value("${app.notifications.min-per-user:10}")
    private int minPerUser;

    /**
     * Vytváří instanci služby pro úklid notifikací.
     *
     * @param notificationRepository repository pro práci s notifikacemi
     * @param clock                  hodiny používané pro získání aktuálního času
     */
    public NotificationCleanupService(NotificationRepository notificationRepository,
                                      Clock clock) {
        this.notificationRepository = notificationRepository;
        this.clock = clock;
    }

    /**
     * Naplánovaný úkol pro odstranění starých notifikací.
     *
     * Úkol je spouštěn denně ve 3:30 ráno (serverového času, respektive
     * podle nastavení CRON výrazu). Metoda:
     * - vypočítá časovou hranici podle retentionDays,
     * - načte staré notifikace seřazené podle uživatele a času vytvoření,
     * - pro každého uživatele ponechá minimálně minPerUser nejnovějších
     *   starých notifikací,
     * - ostatní staré notifikace označí ke smazání a hromadně odstraní.
     *
     * Metoda je transakční, aby bylo zajištěno konzistentní smazání záznamů.
     */
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