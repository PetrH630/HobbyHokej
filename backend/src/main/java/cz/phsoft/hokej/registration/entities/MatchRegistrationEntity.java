package cz.phsoft.hokej.registration.entities;

import cz.phsoft.hokej.registration.enums.ExcuseReason;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující registraci hráče k zápasu.
 *
 * Uchovává informace o účasti hráče na konkrétním zápase,
 * jeho aktuálním stavu registrace, případné omluvě,
 * administrativních poznámkách a přiřazení do týmu.
 *
 * Samostatná entita umožňuje evidenci změn registrace,
 * práci s historií účasti a podporu plánovaných úloh
 * souvisejících s notifikacemi.
 */
@Entity
@Table(name = "match_registrations")
public class MatchRegistrationEntity {

    /**
     * Jednoznačný identifikátor registrace.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Zápas, ke kterému se registrace vztahuje.
     * Vztah je povinný a reprezentuje vlastnickou stranu registrace.
     */
    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    /**
     * Hráč, kterého se registrace týká.
     * Každá registrace náleží právě jednomu hráči.
     */
    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    /**
     * Aktuální stav registrace hráče k zápasu.
     * Hodnota je ukládána jako textová reprezentace výčtového typu.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerMatchStatus status;

    /**
     * Důvod omluvy hráče, pokud je registrace ve stavu omluvené účasti.
     */
    @Enumerated(EnumType.STRING)
    private ExcuseReason excuseReason;

    /**
     * Volitelná textová poznámka k omluvě hráče.
     */
    private String excuseNote;

    /**
     * Administrativní poznámka k registraci.
     * Slouží pro interní evidenci nebo zaznamenání porušení pravidel.
     */
    private String adminNote;

    /**
     * Tým, do kterého je hráč pro daný zápas zařazen.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "team")
    private Team team;

    /**
     * Časové razítko registrace.
     * Používá se například pro určení pořadí přihlášení
     * nebo pro auditní účely.
     */
    @Column(nullable = false, updatable = true)
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Původ vytvoření nebo změny registrace.
     * Typické hodnoty reprezentují uživatelskou nebo systémovou akci.
     */
    @Column(nullable = false, updatable = true)
    private String createdBy;

    /**
     * Pozice hráče v tomto konkrétním zápase.
     * Ukládá se jako textová reprezentace výčtového typu.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "position_in_match", length = 30)
    private PlayerPosition positionInMatch;

    /**
     * Příznak určující, zda již byla odeslána připomínka k zápasu.
     *
     * Slouží k zabránění opakovaného odeslání notifikace
     * stejnému hráči pro stejný zápas.
     */
    @Column(name = "reminder_already_sent", nullable = false)
    private boolean reminderAlreadySent = false;

    /**
     * Vytváří prázdnou instanci entity.
     */
    public MatchRegistrationEntity() {
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public MatchEntity getMatch() { return match; }

    public void setMatch(MatchEntity match) { this.match = match; }

    public PlayerEntity getPlayer() { return player; }

    public void setPlayer(PlayerEntity player) { this.player = player; }

    public PlayerMatchStatus getStatus() { return status; }

    public void setStatus(PlayerMatchStatus status) { this.status = status; }

    public ExcuseReason getExcuseReason() { return excuseReason; }

    public void setExcuseReason(ExcuseReason excuseReason) { this.excuseReason = excuseReason; }

    public String getExcuseNote() { return excuseNote; }

    public void setExcuseNote(String excuseNote) { this.excuseNote = excuseNote; }

    public String getAdminNote() { return adminNote; }

    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public Team getTeam() { return team; }

    public void setTeam(Team team) { this.team = team; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getCreatedBy() { return createdBy; }

    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public boolean isReminderAlreadySent() {
        return reminderAlreadySent;
    }

    public void setReminderAlreadySent(boolean reminderAlreadySent) {
        this.reminderAlreadySent = reminderAlreadySent;
    }

    public PlayerPosition getPositionInMatch() {
        return positionInMatch;
    }

    public void setPositionInMatch(PlayerPosition positionInMatch) {
        this.positionInMatch = positionInMatch;
    }
}