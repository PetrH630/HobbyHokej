package cz.phsoft.hokej.models.services.demo;

import cz.phsoft.hokej.exceptions.DemoModeOperationNotAllowedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
public class DemoModeGuard {

    private final DemoModePolicy policy;

    public DemoModeGuard(DemoModePolicy policy) {
        this.policy = policy;
    }

    /**
     * Ověří, že write operace je povolena. Pokud není povolena,
     * je vyhozena DemoModeOperationNotAllowedException.
     *
     * Používá se tam, kde se nechce psát if podmínka a operace
     * má být v demo režimu zcela zakázána.
     */
    public void write(Long userId, String message) {
        if (policy.isProtectedDemoUser(userId)) {
            throw new DemoModeOperationNotAllowedException(message);
        }
    }

    /**
     * Provede write operaci, pokud je povolena. V opačném případě
     * je vyhozena DemoModeOperationNotAllowedException.
     */
    public void write(Long userId, String message, Runnable action) {
        write(userId, message);
        action.run();
    }

    /**
     * Provede write operaci, pokud je povolena, a vrátí výsledek.
     */
    public <T> T write(Long userId, String message, Supplier<T> action) {
        write(userId, message);
        return action.get();
    }

    /**
     * Provede write operaci, pokud je povolena.
     * Pokud není povolena, provede se finalize část v nové transakci
     * a následně se vyhodí DemoModeOperationNotAllowedException.
     *
     * Používá se pro případy, kdy se má v demo režimu provést
     * "úklid" (například smazání tokenu) a zároveň se má vrátit chyba.
     */
    public void writeWithFinalize(Long userId,
                                  String message,
                                  Runnable writeAction,
                                  Runnable finalizeAction) {

        if (!policy.isProtectedDemoUser(userId)) {
            writeAction.run();
            return;
        }

        runFinalizeInNewTransaction(finalizeAction);

        throw new DemoModeOperationNotAllowedException(message);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void runFinalizeInNewTransaction(Runnable finalizeAction) {
        finalizeAction.run();
    }
}
