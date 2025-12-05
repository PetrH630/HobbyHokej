package cz.phsoft.hokej.models.services.sms;

import org.springframework.stereotype.Component;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class SmsMessageBuilder {

    private final MatchRegistrationRepository matchRegistrationRepository;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public SmsMessageBuilder(MatchRegistrationRepository matchRegistrationRepository) {
        this.matchRegistrationRepository = matchRegistrationRepository;
    }

    // --------------------------
// zpráva po registraci/odhlášení/omluvení
// --------------------------
    public String buildMessageRegistration(MatchRegistrationEntity registration) {
        PlayerMatchStatus status = registration.getStatus();
        String statusText = switch (status) {
            case REGISTERED -> "přihlásil se k zápasu";
            case UNREGISTERED -> "odhlásil se ze zápasu";
            case EXCUSED -> "omluven";
            default -> "neznámý stav";
        };

        Long registeredCount = matchRegistrationRepository
                .countByMatchAndStatus(registration.getMatch(), PlayerMatchStatus.REGISTERED);

        StringBuilder sb = new StringBuilder();
        sb.append("app_hokej - datum: ")
                .append(registration.getMatch().getDateTime().toLocalDate());

        if (status != PlayerMatchStatus.EXCUSED) {
            sb.append(", ").append(registeredCount)
                    .append("/").append(registration.getMatch().getMaxPlayers());
        }

        sb.append(", hráč: ").append(registration.getPlayer().getFullName())
                .append(", status: ").append(statusText);

        return sb.toString();
    }

    // --------------------------
// zpráva pro hráče, kteří ještě nereagovali
// --------------------------
    public String buildMessageNoResponse(PlayerEntity player, MatchEntity match) {
        Long registeredCount = matchRegistrationRepository
                .countByMatchAndStatus(match, PlayerMatchStatus.REGISTERED);

        StringBuilder sb = new StringBuilder();
        sb.append("app_hokej - upozornění: zápas ")
                .append(match.getDateTime().format(dateFormatter))
                .append(" Volných míst: ")
                .append((match.getMaxPlayers()) - (registeredCount))
                .append(". Ještě jste nereagoval ");

        return sb.toString();
    }

    // --------------------------
// finální připomínka pro přihlášené hráče v den zápasu
// --------------------------
    public String buildMessageFinal(MatchRegistrationEntity registration) {
        MatchEntity match = registration.getMatch();
        Long registeredCount = matchRegistrationRepository
                .countByMatchAndStatus(match, PlayerMatchStatus.REGISTERED);

        double pricePerPlayer = match.getPrice() / Math.max(registeredCount, 1);

        StringBuilder sb = new StringBuilder();
        sb.append("app_hokej - připomínka zápasu ")
                .append(match.getDateTime().format(dateFormatter))
                .append(", přihlášeno: ").append(registeredCount).append("/").append(match.getMaxPlayers())
                .append(", cena na hráče: ").append(String.format("%.2f Kč", pricePerPlayer));

        return sb.toString();
    }


}
