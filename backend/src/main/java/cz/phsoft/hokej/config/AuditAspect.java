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

@Component
@Aspect
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger("AUDIT_LOGGER");

    // Pointcut pro všechny metody ve službách
    @Pointcut("within(cz.phsoft.hokej.models.services..*)")
    public void serviceMethods() {}

    // Spustí se před metodou
    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        String args = java.util.Arrays.toString(joinPoint.getArgs());
        logger.info("START {} at {} with args {}", methodName, LocalDateTime.now(), args);
    }

    // Spustí se po úspěšném dokončení metody
    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
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
            } else if (arg instanceof Long id) {
                // pokud má metoda Long parametry, můžeš určit podle pořadí
                // třeba první Long = matchId, druhý = playerId
            }
        }

        // logování
        logger.info("END {} - userId={} playerId={} returned [{}] at {}",
                methodName, userId, playerId, result, LocalDateTime.now());
    }
}

