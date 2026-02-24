// src/pages/InfoPage.jsx
import { Link } from "react-router-dom";
import { useMemo } from "react";
import { useAuth } from "../hooks/useAuth";

/**
 * Informační stránka pro uživatele aplikace.
 *
 * Vysvětluje:
 * - k čemu aplikace slouží,
 * - jaké jsou role,
 * - jak fungují zápasy a registrace,
 * - jak fungují notifikace,
 * - typické scénáře pro hráče / rodiče / manažera / admina.
 */
const InfoPage = () => {
    const { user } = useAuth();

    const rolesLabel = useMemo(() => {
        const roles =
            user?.roles && Array.isArray(user.roles)
                ? user.roles
                : user?.role
                    ? [user.role]
                    : [];
        if (!roles.length) return "Nerozpoznaná role (kontaktuj správce)";
        return roles.join(", ");
    }, [user]);

    return (
        <div className="container py-4">
            {/* Header */}
            <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2 mb-4">
                <div>
                    <h1 className="h3 mb-1">Informace o aplikaci</h1>
                    <p className="text-muted mb-0">
                        Tato stránka ti pomůže pochopit, jak aplikace HobbyHokej funguje
                        a co z toho máš jako uživatel.
                    </p>
                </div>

                <div className="text-muted small text-md-end">
                    {user && (
                        <>
                            Přihlášen:{" "}
                            <span className="fw-semibold">
                                {user.email || user.username || "uživatel"}
                            </span>
                            <br />
                            Role:{" "}
                            <span className="fw-semibold">{rolesLabel}</span>
                        </>
                    )}
                </div>
            </div>

            {/* Co je aplikace */}
            <div className="row g-3 mb-4">
                <div className="col-12 col-lg-7">
                    <div className="card h-100 shadow-sm">
                        <div className="card-body">
                            <h2 className="h5 mb-3">Co je HobbyHokej?</h2>
                            <p className="mb-2">
                                HobbyHokej je aplikace pro organizaci
                                amatérského hokeje (a podobných sportů). Cílem
                                je:
                            </p>
                            <ul className="mb-2">
                                <li>usnadnit přihlašování na zápasy,</li>
                                <li>mít přehled o aktuálním počtu hráčů přihlášených k zápasů</li>
                                <li>mít přehled o hráčích a jejich roli,</li>
                                <li>řešit omluvy a absence přehledně,</li>
                                <li>
                                    posílat notifikace při změnách termínů,
                                    zrušení zápasů apod.
                                </li>
                            </ul>
                            <p className="mb-0">
                                Aplikace má více typů uživatelů - hráče,
                                (rodiče), manažery a administrátory. Každý vidí a
                                může dělat jen to, co odpovídá jeho roli.
                            </p>
                        </div>
                    </div>
                </div>

                {/* Základní orientace / rychlé odkazy */}
                <div className="col-12 col-lg-5">
                    <div className="card h-100 shadow-sm">
                        <div className="card-body">
                            <h2 className="h5 mb-3">Kde co najdu?</h2>
                            <ul className="list-unstyled small mb-3">
                                <li className="mb-1">
                                    <span className="fw-semibold">Zápasy:</span>{" "}
                                    přehled zápasů, přihlášení / odhlášení, detaily
                                    –{" "}
                                    <Link to="/app/matches">
                                        /app/matches
                                    </Link>
                                </li>
                                <li className="mb-1">
                                    <span className="fw-semibold">
                                        Moji hráči:
                                    </span>{" "}
                                    přepínání mezi hráči (např. dítě/rodič),
                                    správa profilu –{" "}
                                    <Link to="/app/players">
                                        /app/players
                                    </Link>
                                </li>
                                <li className="mb-1">
                                    <span className="fw-semibold">
                                        Moje absence:
                                    </span>{" "}
                                    nahlášení, že nebudeš nějaké období hrát –{" "}
                                    <Link to="/app/my-inactivity">
                                        /app/my-inactivity
                                    </Link>
                                </li>
                                <li className="mb-1">
                                    <span className="fw-semibold">
                                        Nastavení:
                                    </span>{" "}
                                    email/SMS upozornění, jazyk, časové pásmo –
                                    <Link to="/app/settings">
                                        {" "}
                                        /app/settings
                                    </Link>
                                </li>
                                <li className="mb-1">
                                    <span className="fw-semibold">
                                        Správa (pro manažery/adminy):
                                    </span>{" "}
                                    sezóny, zápasy, hráči, uživatelé –{" "}
                                    <Link to="/app/admin">
                                        /app/admin
                                    </Link>
                                </li>
                            </ul>

                            <p className="text-muted mb-0">
                                Tip: Kdykoli se ztratíš, klikni na{" "}
                                <span className="fw-semibold">Přehled</span>{" "}
                                v menu – tam se vždy vrátíš na úvodní stránku
                                pro hráče.
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Role v systému */}
            <div className="card shadow-sm mb-4">
                <div className="card-body">
                    <h2 className="h5 mb-3">Role v systému</h2>
                    <div className="row g-3">
                        <div className="col-12 col-md-6 col-xl-3">
                            <h3 className="h6">Hráč</h3>
                            <ul className="small mb-2">
                                <li>Vidí své zápasy a přihlášky.</li>
                                <li>Může se přihlásit / odhlásit na zápas.</li>
                                <li>Vidí svoje statistiky (účast apod.).</li>
                                <li>Delší absenci zadává pouze Admin/Manažer.</li>
                            </ul>
                        </div>
                        <div className="col-12 col-md-6 col-xl-3">
                            <h3 className="h6">Rodič / zástupce</h3>
                            <ul className="small mb-2">
                                <li>
                                    Má účet v systému, ale přepíná mezi „svými“
                                    hráči (např. děti).
                                </li>
                                <li>
                                    Přihlašuje/odhlašuje hráče na zápasy za ně.
                                </li>
                                <li>Řeší omluvy.</li>
                            </ul>
                        </div>
                        <div className="col-12 col-md-6 col-xl-3">
                            <h3 className="h6">Manažer</h3>
                            <ul className="small mb-2">
                                <li>Plánuje zápasy (čas, místo, kapacita).</li>
                                <li>Schvaluje hráče a jejich status.</li>
                                <li>
                                    Kontroluje přihlášky a řeší náhradníky /
                                    čekací listina je řešena automaticky systémem dle data přihlášení k zápasu.
                                </li>
                                <li>
                                    Vidí přehled notifikací a změn v systému.
                                </li>
                            </ul>
                        </div>
                        <div className="col-12 col-md-6 col-xl-3">
                            <h3 className="h6">Admin</h3>
                            <ul className="small mb-2">
                                <li>Spravuje uživatele a jejich role.</li>
                                <li>Spravuje sezóny a globální nastavení.</li>
                                <li>Dohlíží na notifikace a logy.</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>

            {/* Typické scénáře */}
            <div className="card shadow-sm mb-4">
                <div className="card-body">
                    <h2 className="h5 mb-3">Typické scénáře (jak na to)</h2>

                    <h3 className="h6 mb-2">Jsem hráč / rodič</h3>
                    <ol className="small mb-3">
                        <li className="mb-2">
                            <span className="fw-semibold">
                                Chci se přihlásit na zápas
                            </span>
                            <ul className="mb-1">
                                <li>
                                    Otevři{" "}
                                    <Link to="/app/matches">
                                        Zápasy
                                    </Link>
                                    .
                                </li>
                                <li>
                                    Najdi konkrétní zápas a klikni na něj{" "}
                                    <span className="fw-semibold">
                                        a na další stránce se přihlas pomocí tlačítka
                                    </span>{" "}
                                    (text je různý podle statutu aktuálně přihlášeného hráče a dostatečně vystihuje volbu pro přihlášení k zápasu).
                                </li>
                                <li>
                                    Pokud je zápas plný, můžeš se dostat na
                                    čekací listinu / náhradníka.
                                </li>
                            </ul>
                        </li>

                        <li className="mb-2">
                            <span className="fw-semibold">
                                Potřebuji se ze zápasu odhlásit
                            </span>
                            <ul className="mb-1">
                                <li>
                                    Opět{" "}
                                    <Link to="/app/matches">
                                        Zápasy
                                    </Link>{" "}
                                    → vyber konkrétní zápas.
                                </li>
                                <li>
                                    Klikni na{" "}
                                    <span className="fw-semibold">
                                        příslušné tlačítko
                                    </span>{" "}
                                    (text je různý podle statutu aktuálně přihlášeného hráče a dostatečně vystihuje volbu pro odhlášení ze zápasu).
                                </li>
                                <li>
                                    Pokud je někdo na čekací listině jako náhradník, bude automaticky
                                    zařazen do sestavy.
                                </li>
                            </ul>
                        </li>

                        <li className="mb-2">
                            <span className="fw-semibold">
                                Vím, že teď delší dobu hrát nebudu (zranění,
                                dovolená… kontaktuj managera a on doplní tvou neaktivitu do systému)
                            </span>
                            <ul className="mb-1">
                                <li>
                                    To potom uvidíš když otevřeš {" "}
                                    <Link to="/app/my-inactivity">
                                        Mimo
                                    </Link>
                                    .
                                </li>
                                <li>
                                    Zde bude zadáno období, kdy nebudeš k dispozici.
                                </li>
                                <li>
                                    Manažer uvidí tvoji absenci a nebudeš se moci přihlásit k zápasu, 
                                    a v daném období se s tebou nebude počítat.
                                </li>
                            </ul>
                        </li>

                        <li className="mb-2">
                            <span className="fw-semibold">
                                Chci si upravit notifikace (email/SMS)
                            </span>
                            <ul className="mb-1">
                                <li>
                                    Otevři{" "}
                                    <Link to="/app/settings">
                                        Nastavení
                                    </Link>
                                    .
                                </li>
                                <li>
                                    Uprav, jaké typy upozornění chceš dostávat
                                    (např. změna data/času zápasu, zrušení zápasu, tvojé registrace na zápas...).
                                </li>
                            </ul>
                        </li>
                    </ol>

                    <h3 className="h6 mb-2">Jsem manažer</h3>
                    <ol className="small mb-3">
                        <li className="mb-2">
                            <span className="fw-semibold">
                                Chci vytvořit nový zápas (kliknu na Správa)
                            </span>
                            <ul className="mb-1">
                                <li>
                                    Otevři{" "}
                                    <Link to="/app/admin/matches">
                                        Admin &rarr; Zápasy
                                    </Link>
                                    .
                                </li>
                                <li>
                                    Vytvořit nový zápas (datum, čas, místo, popis (volitelné)
                                    maximální počet hráčů, cena (za pronájem haly)).
                                </li>
                                <li>
                                    Po uložení dostanou hráči notifikaci podle
                                    svého nastavení.
                                </li>
                            </ul>
                        </li>

                        <li className="mb-2">
                            <span className="fw-semibold">
                                Potřebuji změnit čas nebo místo zápasu
                            </span>
                            <ul className="mb-1">
                                <li>
                                    Najdi zápas v{" "}
                                    <Link to="/app/admin/matches">
                                        Admin &rarr; Zápasy
                                    </Link>
                                    .
                                </li>
                                <li>Uprav čas/místo a ulož změny.</li>
                                <li>
                                    Systém automaticky pošle notifikace
                                    přihlášeným hráčům.
                                </li>
                            </ul>
                        </li>

                        <li className="mb-2">
                            <span className="fw-semibold">
                                Chci schválit nového hráče
                            </span>
                            <ul className="mb-1">
                                <li>
                                    Otevři{" "}
                                    <Link to="/app/admin/players">
                                        Admin &rarr; Hráči
                                    </Link>
                                    .
                                </li>
                                <li>
                                    Najdi hráče ve stavu „čeká na schválení“ a
                                    změň jeho status na „aktivní“.
                                </li>
                            </ul>
                        </li>
                    </ol>

                    <h3 className="h6 mb-2">Jsem admin</h3>
                    <ol className="small mb-0">
                        <li className="mb-2">
                            <span className="fw-semibold">
                                Chci vytvořit nového uživatele nebo nastavit
                                role
                            </span>
                            <ul className="mb-1">
                                <li>
                                    Otevři{" "}
                                    <Link to="/app/admin/users">
                                        Admin &rarr; Uživatelé
                                    </Link>
                                    .
                                </li>
                                <li>
                                    Přidej nového uživatele nebo uprav role
                                    existujícímu (PLAYER, MANAGER, ADMIN…).
                                </li>
                            </ul>
                        </li>

                        <li className="mb-2">
                            <span className="fw-semibold">
                                Chci zkontrolovat notifikace v systému
                            </span>
                            <ul className="mb-1">
                                <li>
                                    Otevři{" "}
                                    <Link to="/app/admin/notifications">
                                        Admin &rarr; Notifikace
                                    </Link>
                                    .
                                </li>
                                <li>
                                    Zde vidíš, jaké notifikace byly
                                    odesílány/ukládány a kterým uživatelům.
                                </li>
                            </ul>
                        </li>
                    </ol>
                </div>
            </div>

            {/* Shrnutí / podpora */}
            <div className="text-muted small">
                Pokud ti v aplikaci něco nefunguje nebo něčemu nerozumíš,
                obrať se na svého manažera nebo administrátora.
                V budoucnu se sem mohou přidat i odkazy na návody, FAQ a
                kontaktní informace.
            </div>
        </div>
    );
};

export default InfoPage;