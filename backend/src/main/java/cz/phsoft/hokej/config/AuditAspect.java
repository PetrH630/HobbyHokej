package cz.phsoft.hokej.config;

import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * AuditAspect
 *
 * CROSS-CUTTING CONCERN:
 * ----------------------
 * Tento aspekt slouží k centrálnímu auditnímu logování volání
 * service vrstev aplikace.
 *
 * CO LOGUJE:
 * ----------
 * - začátek volání metody (název + argumenty)
 * - konec úspěšného volání metody
 * - návratovou hodnotu
 * - časové razítko
 *
 * ROZSAH:
 * -------
 * - všechny metody ve všech třídách v balíčku
 *   cz.phsoft.hokej.models.services..*
 *
 * PROČ AOP:
 * ----------
 * - auditní logika není roztroušena po službách
 * - žádné duplikace kódu
 * - snadné zapnutí / vypnutí / úpravy
 *
 * BEZPEČNOST:
 * -----------
 * - aspekt NEMĚNÍ chování aplikace
 * - pouze čte data a loguje
 * - v případě chyby v logování aplikace pokračuje dál
 */
@Component
@Aspect
public class AuditAspect {

    /**
     * Speciální logger určený pouze pro auditní záznamy.
     *
     * Doporučení:
     * - v logback.xml / log4j2.xml mít samostatný appender (soubor)
     * - oddělit auditní logy od aplikačních logů
     */
    private static final Logger logger = LoggerFactory.getLogger("AUDIT_LOGGER");

    // =====================================================
    // POINTCUT
    // =====================================================

    /**
     * Pointcut definující všechny metody ve service vrstvě.
     *
     * Zahrnuje:
     * - všechny třídy
     * - všechny metody
     * - včetně podbalíčků
     */
    @Pointcut("within(cz.phsoft.hokej.models.services..*)")
    public void serviceMethods() {
        // pouze marker metoda pro pointcut
    }

    // =====================================================
    // BEFORE ADVICE
    // =====================================================

    /**
     * Spustí se PŘED zavoláním jakékoli service metody.
     *
     * Slouží pro:
     * - záznam začátku operace
     * - debug / audit časování
     *
     * @param joinPoint kontext volané metody
     */
    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {

        String methodName = joinPoint.getSignature().toShortString();
        String args = java.util.Arrays.toString(joinPoint.getArgs());

        logger.info(
                "START {} at {} with args {}",
                methodName,
                LocalDateTime.now(),
                args
        );
    }

    // =====================================================
    // AFTER RETURNING ADVICE
    // =====================================================

    /**
     * Spustí se PO ÚSPĚŠNÉM dokončení metody
     * (NEspustí se při vyhození výjimky).
     *
     * Slouží pro:
     * - audit úspěšných operací
     * - logování návratových hodnot
     *
     * @param joinPoint kontext volané metody
     * @param result    návratová hodnota metody
     */
    @AfterReturning(
            pointcut = "serviceMethods()",
            returning = "result"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {

        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        Long userId = null;
        Long playerId = null;

        /*
         * Pokus o extrakci zajímavých business identifikátorů
         * z parametrů metody.
         *
         * Cílem je mít auditní stopu:
         * - KTERÝ HRÁČ
         * - JAKÁ OPERACE
         */
        for (Object arg : args) {
            if (arg instanceof PlayerEntity player) {
                playerId = player.getId();
            } else if (arg instanceof MatchRegistrationEntity registration) {
                playerId = registration.getPlayer().getId();
            } else if (arg instanceof Long id) {
                // zde můžeš případně rozlišovat:
                // - první Long = matchId
                // - druhý Long = playerId
                // dle konvence signatur metod
            }
        }

        logger.info(
                "END {} - userId={} playerId={} returned [{}] at {}",
                methodName,
                userId,
                playerId,
                result,
                LocalDateTime.now()
        );
    }
}
