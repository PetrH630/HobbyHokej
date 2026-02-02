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
 * Aspekt pro auditní logování service vrstvy.
 *
 * Používá se k centrálnímu zaznamenávání volání metod ve službách
 * včetně argumentů, návratových hodnot a časových razítek. Chování
 * aplikace se tímto aspektem nemění, pouze se doplňují auditní logy
 * do samostatného loggeru.
 */
@Component
@Aspect
public class AuditAspect {

    /**
     * Speciální logger určený pouze pro auditní záznamy.
     *
     * Doporučuje se mít pro tento logger samostatný appender
     * a oddělený soubor logu, aby byly auditní záznamy odděleny
     * od běžných aplikačních logů.
     */
    private static final Logger logger = LoggerFactory.getLogger("AUDIT_LOGGER");

    // Pointcut pro metody service vrstvy

    /**
     * Pointcut definující všechny metody ve service vrstvě aplikace.
     *
     * Zahrnuje všechny třídy a metody v balíčku
     * {@code cz.phsoft.hokej.models.services..} včetně podbalíčků.
     */
    @Pointcut("within(cz.phsoft.hokej.models.services..*)")
    public void serviceMethods() {
        // Marker metoda pro pointcut
    }

    // Logování před voláním metody

    /**
     * Provádí auditní záznam před zavoláním jakékoli service metody.
     *
     * Zapisuje název metody, argumenty a aktuální čas. Slouží k evidenci
     * začátku provádění operace a k pozdější analýze průběhu volání.
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

    // Logování po úspěšném dokončení metody

    /**
     * Provádí auditní záznam po úspěšném dokončení metody.
     *
     * Metoda se nespouští při vyhození výjimky. Zapisuje název metody,
     * případné identifikátory hráče a návratovou hodnotu včetně času
     * ukončení operace.
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

        // Pokus o extrakci business identifikátorů z parametrů metody
        for (Object arg : args) {
            if (arg instanceof PlayerEntity player) {
                playerId = player.getId();
            } else if (arg instanceof MatchRegistrationEntity registration) {
                playerId = registration.getPlayer().getId();
            } else if (arg instanceof Long id) {
                // Případné rozlišení konkrétního ID lze doplnit podle konvence signatur metod
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
