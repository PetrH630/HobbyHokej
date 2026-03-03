package cz.phsoft.hokej.demo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * Guard služba pro blokování write operací v demo režimu.
 *
 * Třída centralizuje kontrolu pravidel demo režimu tak, aby se podmínky
 * nepsaly opakovaně ve service vrstvě. Při pokusu o nepovolenou operaci
 * se vyhazuje DemoModeOperationNotAllowedException. Kontrola se vyhodnocuje
 * na základě DemoModePolicy, která určuje, zda je daný uživatel považován
 * za chráněného demo uživatele.
 */
@Service
public class DemoModeGuard {

    private final DemoModePolicy policy;

    /**
     * Vytváří guard pro demo režim.
     *
     * @param policy politika určující chování aplikace v demo režimu
     */
    public DemoModeGuard(DemoModePolicy policy) {
        this.policy = policy;
    }

    /**
     * Provede kontrolu, zda je write operace v demo režimu povolena.
     *
     * Pokud je uživatel vyhodnocen jako chráněný demo uživatel, operace se
     * blokuje vyhozením DemoModeOperationNotAllowedException.
     *
     * @param userId identifikátor uživatele, pro kterého se kontrola vyhodnocuje
     * @param message chybová zpráva použitá při blokaci operace
     */
    public void write(Long userId, String message) {
        if (policy.isProtectedDemoUser(userId)) {
            throw new DemoModeOperationNotAllowedException(message);
        }
    }

    /**
     * Provede write operaci, pokud je v demo režimu povolena.
     *
     * Pokud je uživatel vyhodnocen jako chráněný demo uživatel, operace se
     * neprovede a vyhodí se DemoModeOperationNotAllowedException.
     *
     * @param userId identifikátor uživatele, pro kterého se kontrola vyhodnocuje
     * @param message chybová zpráva použitá při blokaci operace
     * @param action akce reprezentující write operaci
     */
    public void write(Long userId, String message, Runnable action) {
        write(userId, message);
        action.run();
    }

    /**
     * Provede write operaci, pokud je v demo režimu povolena, a vrátí její výsledek.
     *
     * Pokud je uživatel vyhodnocen jako chráněný demo uživatel, operace se
     * neprovede a vyhodí se DemoModeOperationNotAllowedException.
     *
     * @param userId identifikátor uživatele, pro kterého se kontrola vyhodnocuje
     * @param message chybová zpráva použitá při blokaci operace
     * @param action akce reprezentující write operaci vracející výsledek
     * @param <T> typ návratové hodnoty write operace
     * @return výsledek provedené write operace
     */
    public <T> T write(Long userId, String message, Supplier<T> action) {
        write(userId, message);
        return action.get();
    }

    /**
     * Provede write operaci, pokud je v demo režimu povolena, nebo vyvolá blokaci s finalize krokem.
     *
     * Pokud je uživatel vyhodnocen jako chráněný demo uživatel, write operace se neprovede.
     * Následně se provede finalize část v nové transakci a poté se vyhodí
     * DemoModeOperationNotAllowedException.
     *
     * Metoda se používá pro situace, kdy se má v demo režimu provést úklidová akce,
     * například odstranění reset tokenu, a současně se má volajícímu vrátit chyba.
     *
     * @param userId identifikátor uživatele, pro kterého se kontrola vyhodnocuje
     * @param message chybová zpráva použitá při blokaci operace
     * @param writeAction akce reprezentující standardní write operaci
     * @param finalizeAction akce reprezentující úklidovou operaci, která se má provést i v demo režimu
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

    /**
     * Provede finalize operaci v nové transakci.
     *
     * Nová transakce se používá pro zajištění, že finalize operace bude provedena
     * nezávisle na transakčním kontextu volající write operace, která je následně blokována.
     *
     * @param finalizeAction akce, která se má provést v nové transakci
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void runFinalizeInNewTransaction(Runnable finalizeAction) {
        finalizeAction.run();
    }
}