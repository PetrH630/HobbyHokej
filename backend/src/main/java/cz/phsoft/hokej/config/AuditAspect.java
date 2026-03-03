package cz.phsoft.hokej.config;

import cz.phsoft.hokej.registration.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
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
 * Aspekt pro auditní logování service vrstvy.
 *
 * Používá se k centrálnímu zaznamenávání volání metod ve službách,
 * včetně argumentů, návratových hodnot a časových razítek. Aplikační
 * logika není tímto aspektem ovlivněna, pouze se vytváří auditní záznam.
 */
@Component
@Aspect
public class AuditAspect {

    /**
     * Logger určený pro auditní záznamy.
     *
     * Doporučuje se mít pro tento logger samostatný appender a oddělený
     * soubor logu, aby byly auditní záznamy odděleny od běžných logů.
     */
    private static final Logger logger = LoggerFactory.getLogger("AUDIT_LOGGER");

    /**
     * Pointcut definující všechny metody ve service vrstvě aplikace.
     *
     * Zahrnuje všechny třídy a metody v balíčku
     * cz.phsoft.hokej.models.services a jeho podbalíčcích.
     */
    @Pointcut("within(cz.phsoft.hokej.models.services..*)")
    public void serviceMethods() {
        // marker metoda pro pointcut
    }

    /**
     * Provádí auditní záznam před zavoláním service metody.
     *
     * Zapisuje název metody, argumenty a čas zahájení operace.
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

    /**
     * Provádí auditní záznam po úspěšném dokončení metody.
     *
     * Metoda se nespouští při vyhození výjimky. Zapisuje název metody,
     * identifikátory relevantních entit a návratovou hodnotu.
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

        for (Object arg : args) {
            if (arg instanceof PlayerEntity player) {
                playerId = player.getId();
            } else if (arg instanceof MatchRegistrationEntity registration) {
                playerId = registration.getPlayer().getId();
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