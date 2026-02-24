package cz.phsoft.hokej.config;

import cz.phsoft.hokej.data.entities.*;
import cz.phsoft.hokej.data.enums.*;
import cz.phsoft.hokej.data.repositories.*;
import cz.phsoft.hokej.models.services.AppUserSettingsService;
import cz.phsoft.hokej.models.services.PlayerSettingsService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Komponenta pro inicializaci ukázkových dat v databázi.
 *
 * Po startu aplikace se vytváří výchozí administrátor, ukázkoví hráči s uživateli,
 * výchozí nastavení uživatelů a hráčů, sezóny, zápasy a ukázkové registrace.
 *
 * Inicializace se spouští pouze při zapnutí vlastnosti {@code app.seed.enabled=true}.
 * Databázové triggery se v této třídě nevytvářejí, protože se spravují pomocí Flyway migrací.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = false)
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final MatchRegistrationRepository matchRegistrationRepository;
    private final AppUserRepository appUserRepository;
    private final SeasonRepository seasonRepository;
    private final AppUserSettingsRepository appUserSettingsRepository;
    private final PlayerSettingsRepository playerSettingsRepository;
    private final AppUserSettingsService appUserSettingService;
    private final PlayerSettingsService playerSettingService;
    private final JdbcTemplate jdbcTemplate;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataInitializer(PlayerRepository playerRepository,
                           MatchRepository matchRepository,
                           MatchRegistrationRepository matchRegistrationRepository,
                           AppUserRepository appUserRepository,
                           SeasonRepository seasonRepository,
                           AppUserSettingsRepository appUserSettingsRepository,
                           PlayerSettingsRepository playerSettingsRepository,
                           AppUserSettingsService appUserSettingService,
                           PlayerSettingsService playerSettingService,
                           JdbcTemplate jdbcTemplate) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.matchRegistrationRepository = matchRegistrationRepository;
        this.appUserRepository = appUserRepository;
        this.seasonRepository = seasonRepository;
        this.appUserSettingsRepository = appUserSettingsRepository;
        this.playerSettingsRepository = playerSettingsRepository;
        this.appUserSettingService = appUserSettingService;
        this.playerSettingService = playerSettingService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Spouští inicializaci ukázkových dat po startu aplikace.
     *
     * Metody se volají v pořadí tak, aby byly zachovány závislosti mezi entitami.
     * Triggery se nevytvářejí, protože se spravují pomocí Flyway.
     */
    @PostConstruct
    public void init() {
        log.info("DataInitializer is enabled. Demo/test data initialization starts.");

        initAdmin();
        initPlayersAndUsers();
        initUserSettings();
        initPlayerSettings();
        initSeasons();
        initMatches();
        initRegistrations();

        log.info("Data initialization completed.");
    }

    /**
     * Vytváří výchozího administrátora, pokud ještě neexistuje.
     *
     * Timestamp se po uložení upraví přes JdbcTemplate, protože AppUserEntity nastavuje
     * timestamp při persistenci automaticky.
     */
    private void initAdmin() {
        appUserRepository.findByEmail("admin@example.com").ifPresentOrElse(
                existing -> log.info("Admin user already exists. Initialization is skipped."),
                () -> {
                    AppUserEntity admin = new AppUserEntity();
                    admin.setName("admin");
                    admin.setSurname("admin");
                    admin.setEmail("admin@example.com");
                    admin.setPassword(encoder.encode("Administrator123"));
                    admin.setRole(Role.ROLE_ADMIN);
                    admin.setEnabled(true);

                    appUserRepository.save(admin);

                    LocalDateTime fixedTimestamp = LocalDateTime.of(2024, 11, 1, 8, 0);
                    jdbcTemplate.update(
                            "UPDATE app_users SET timestamp = ? WHERE id = ?",
                            fixedTimestamp,
                            admin.getId()
                    );

                    log.info("Default admin user has been created (id={}).", admin.getId());
                }
        );
    }

    /**
     * Vytváří testovací hráče a k nim přiřazené uživatele.
     *
     * Pokud již v databázi existují hráči, inicializace se přeskočí.
     */
    private void initPlayersAndUsers() {
        if (playerRepository.count() > 0) {
            log.info("Players already exist. Player initialization is skipped.");
            return;
        }

        String[] names = {
                "Jan", "Petr", "Jiří", "Josef", "Pavel",
                "Martin", "Tomáš", "Jaroslav", "Miroslav", "Zdeněk"
        };
        String[] surnames = {
                "Novák", "Svoboda", "Novotný", "Dvořák", "Černý",
                "Procházka", "Kučera", "Veselý", "Horák", "Němec"
        };

        for (int i = 0; i < 10; i++) {
            PlayerEntity player = new PlayerEntity();
            AppUserEntity user = new AppUserEntity();

            player.setName(names[i]);
            user.setName(names[i]);

            player.setSurname(surnames[i].toUpperCase());
            user.setSurname(surnames[i].toUpperCase());

            String email = "player" + (i + 1) + "@example.com";
            user.setEmail(email);
            user.setPassword(encoder.encode("Heslo123"));

            switch (i) {
                case 0, 1, 2 -> player.setType(PlayerType.VIP);
                case 3, 4, 5, 6 -> player.setType(PlayerType.STANDARD);
                default -> player.setType(PlayerType.BASIC);
            }

            player.setPhoneNumber("");
            player.setTeam(i < 5 ? Team.DARK : Team.LIGHT);
            player.setPlayerStatus(i < 8 ? PlayerStatus.APPROVED : PlayerStatus.PENDING);

            user.setRole(i == 0 ? Role.ROLE_MANAGER : Role.ROLE_PLAYER);
            user.setEnabled(true);

            player.setUser(user);

            LocalDateTime randomTs = randomTimestampForDemoData();
            player.setTimestamp(randomTs);

            appUserRepository.save(user);
            playerRepository.save(player);

            jdbcTemplate.update(
                    "UPDATE app_users SET timestamp = ? WHERE id = ?",
                    randomTs,
                    user.getId()
            );
        }

        log.info("Players and users have been initialized.");
    }

    /**
     * Vytváří výchozí nastavení pro všechny uživatele, kteří je ještě nemají.
     */
    private void initUserSettings() {
        log.info("User settings initialization starts.");

        List<AppUserEntity> users = appUserRepository.findAll();
        for (AppUserEntity user : users) {
            boolean hasSettings = appUserSettingsRepository.existsByUser(user);
            if (hasSettings) {
                continue;
            }

            AppUserSettingsEntity settings =
                    appUserSettingService.createDefaultSettingsForUser(user);

            appUserSettingsRepository.save(settings);
        }

        log.info("User settings have been initialized.");
    }

    /**
     * Vytváří výchozí nastavení pro všechny hráče, kteří je ještě nemají.
     */
    private void initPlayerSettings() {
        log.info("Player settings initialization starts.");

        List<PlayerEntity> players = playerRepository.findAll();
        for (PlayerEntity player : players) {
            boolean hasSettings = playerSettingsRepository.existsByPlayer(player);
            if (hasSettings) {
                continue;
            }

            PlayerSettingsEntity settings =
                    playerSettingService.createDefaultSettingsForPlayer(player);

            playerSettingsRepository.save(settings);
        }

        log.info("Player settings have been initialized.");
    }

    /**
     * Vytváří výchozí sezóny, pokud žádné neexistují.
     */
    private void initSeasons() {
        if (seasonRepository.count() > 0) {
            log.info("Seasons already exist. Season initialization is skipped.");
            return;
        }

        log.info("Season initialization starts.");

        SeasonEntity season2024_2025 = new SeasonEntity();
        season2024_2025.setName("2024/2025");
        season2024_2025.setStartDate(LocalDate.of(2024, 11, 1));
        season2024_2025.setEndDate(LocalDate.of(2025, 3, 31));
        season2024_2025.setActive(false);
        season2024_2025.setCreatedByUserId(2L);

        SeasonEntity season2025_2026 = new SeasonEntity();
        season2025_2026.setName("2025/2026");
        season2025_2026.setStartDate(LocalDate.of(2025, 11, 1));
        season2025_2026.setEndDate(LocalDate.of(2026, 5, 31));
        season2025_2026.setActive(true);
        season2025_2026.setCreatedByUserId(2L);

        SeasonEntity season2026_2027 = new SeasonEntity();
        season2026_2027.setName("2026/2027");
        season2026_2027.setStartDate(LocalDate.of(2026, 11, 1));
        season2026_2027.setEndDate(LocalDate.of(2027, 5, 31));
        season2026_2027.setActive(false);
        season2026_2027.setCreatedByUserId(2L);

        seasonRepository.saveAll(List.of(season2024_2025, season2025_2026, season2026_2027));

        log.info("Seasons have been initialized.");
    }

    /**
     * Vytváří zápasy pro první dvě sezóny.
     *
     * Zápasy se generují po pátcích v rámci období sezóny.
     * Pokud již existují zápasy, inicializace se přeskočí.
     */
    private void initMatches() {
        if (matchRepository.count() > 0) {
            log.info("Matches already exist. Match initialization is skipped.");
            return;
        }

        List<SeasonEntity> seasons = seasonRepository.findAll();
        if (seasons.isEmpty()) {
            throw new IllegalStateException("Nelze inicializovat zápasy, protože neexistuje žádná sezóna.");
        }

        log.info("Match initialization starts.");

        for (int j = 0; j < 2 && j < seasons.size(); j++) {
            SeasonEntity actualSeason = seasons.get(j);

            LocalDate startSeasonDate = actualSeason.getStartDate();
            LocalDate endSeasonDate = actualSeason.getEndDate();

            LocalDateTime firstMatchDate = startSeasonDate
                    .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
                    .atTime(18, 45);

            int fridaysCount = countFridays(startSeasonDate, endSeasonDate);

            for (int i = 0; i < fridaysCount; i++) {
                MatchEntity match = new MatchEntity();
                match.setDateTime(firstMatchDate.plusWeeks(i));
                match.setLocation("NĚJAKÁ HALA");
                match.setDescription("");
                match.setMaxPlayers(12);
                match.setPrice(2200);
                match.setMatchStatus(null);
                match.setCancelReason(null);
                match.setSeason(actualSeason);
                match.setCreatedByUserId(2L);

                matchRepository.save(match);
            }
        }

        log.info("Matches have been initialized.");
    }

    /**
     * Vytváří ukázkové registrace hráčů na zápasy.
     *
     * Registrace se vytvářejí pouze pro zápasy v blízké budoucnosti.
     * Pokud již registrace existují, inicializace se přeskočí.
     */
    private void initRegistrations() {
        if (matchRegistrationRepository.count() > 0) {
            log.info("Match registrations already exist. Registration initialization is skipped.");
            return;
        }

        LocalDateTime finalDate = LocalDateTime.now().plusWeeks(1);

        List<MatchEntity> matches = matchRepository.findAll().stream()
                .filter(m -> m.getDateTime().isBefore(finalDate))
                .toList();

        List<PlayerEntity> players = playerRepository.findAll().stream()
                .filter(p -> p.getId() != null)
                .filter(p -> p.getPlayerStatus() == PlayerStatus.APPROVED)
                .toList();

        if (matches.isEmpty() || players.size() < 6) {
            log.info("Not enough data for registration initialization. Initialization is skipped.");
            return;
        }

        for (MatchEntity match : matches) {
            List<PlayerEntity> shuffledPlayers = new ArrayList<>(players);
            Collections.shuffle(shuffledPlayers);
            List<PlayerEntity> selectedPlayers = shuffledPlayers.subList(0, 8);

            List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < selectedPlayers.size(); i++) {
                indexes.add(i);
            }
            Collections.shuffle(indexes);

            int excusedIndex = indexes.get(0);
            int unregisteredIndex1 = indexes.get(1);
            int unregisteredIndex2 = indexes.get(2);

            for (int i = 0; i < selectedPlayers.size(); i++) {
                PlayerEntity player = selectedPlayers.get(i);

                MatchRegistrationEntity reg = new MatchRegistrationEntity();
                reg.setMatch(match);
                reg.setPlayer(player);

                if (i == excusedIndex) {
                    reg.setStatus(PlayerMatchStatus.EXCUSED);
                    reg.setExcuseReason(ExcuseReason.NEMOC);
                    reg.setExcuseNote("chřipka");
                } else if (i == unregisteredIndex1 || i == unregisteredIndex2) {
                    reg.setStatus(PlayerMatchStatus.UNREGISTERED);
                    reg.setExcuseReason(null);
                    reg.setExcuseNote(null);
                } else {
                    reg.setStatus(PlayerMatchStatus.REGISTERED);
                    reg.setExcuseReason(null);
                    reg.setExcuseNote(null);
                }

                reg.setTeam(player.getTeam());
                reg.setTimestamp(LocalDateTime.now());
                reg.setCreatedBy("initializer");

                matchRegistrationRepository.save(reg);
            }
        }

        log.info("Match registrations have been initialized.");
    }

    /**
     * Spočítá počet pátků v období včetně.
     *
     * @param from počáteční datum
     * @param to koncové datum
     * @return počet pátků v období
     */
    private int countFridays(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            return 0;
        }

        LocalDate firstFriday = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        if (firstFriday.isAfter(to)) {
            return 0;
        }

        int count = 0;
        for (LocalDate date = firstFriday; !date.isAfter(to); date = date.plusWeeks(1)) {
            count++;
        }
        return count;
    }

    /**
     * Vygeneruje náhodný timestamp v rozsahu určeném pro ukázková data.
     *
     * @return náhodný timestamp v UTC
     */
    private LocalDateTime randomTimestampForDemoData() {
        LocalDateTime from = LocalDateTime.of(2024, 11, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2025, 2, 10, 23, 59);

        long fromEpoch = from.toEpochSecond(ZoneOffset.UTC);
        long toEpoch = to.toEpochSecond(ZoneOffset.UTC);

        long randomEpoch = ThreadLocalRandom.current().nextLong(fromEpoch, toEpoch + 1);
        return LocalDateTime.ofEpochSecond(randomEpoch, 0, ZoneOffset.UTC);
    }
}
