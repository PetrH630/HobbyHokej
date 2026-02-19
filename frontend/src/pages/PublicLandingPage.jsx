import { Link } from "react-router-dom";

const PublicLandingPage = () => {
    return (
        <div className="min-vh-100 d-flex flex-column bg-light">

            {/* TOP BAR */}
            <div className="border-bottom bg-white shadow-sm">
                <div className="container py-3">

                    <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center">

                        {/* ===== LEVÁ ČÁST ===== */}
                        <div className="d-flex flex-column flex-md-row align-items-start align-items-md-center gap-2">

                            {/* Logo + název pod sebou na mobile */}
                            <div className="d-flex flex-column flex-md-row align-items-start align-items-md-center gap-2">
                                <img
                                    src="/hockey-clipart.svg"
                                    alt="Logo"
                                    style={{ height: "34px" }}
                                />

                                <div className="fw-bold fs-5">
                                    HobbyHokej App
                                </div>
                            </div>

                            {/* Badge DEMO */}
                            <span className="badge bg-danger">
                                DEMO
                            </span>
                        </div>

                        {/* ===== PRAVÁ ČÁST ===== */}
                        <div className="d-flex flex-column flex-md-row gap-2 mt-3 mt-md-0">
                            <Link
                                to="/login"
                                className="btn btn-primary btn-sm px-3"
                            >
                                Přihlášení
                            </Link>

                            <Link
                                to="/register"
                                className="btn btn-outline-primary btn-sm px-3"
                            >
                                Registrace
                            </Link>
                        </div>

                    </div>
                </div>
            </div>


            {/* HERO */}
            <main className="flex-grow-1">

                <div
                    className="py-5"
                    style={{
                        background: "linear-gradient(135deg, #f8f9fa 0%, #e9f2ff 100%)"
                    }}
                >
                    <div className="container">
                        <div className="row align-items-center g-5">

                            {/* LEFT */}
                            <div className="col-lg-7">

                                <div className="mb-4 d-flex justify-content-center">
                                    <img
                                        src="/hockey-clipart.svg"
                                        alt="Logo"
                                        className="img-fluid"
                                        style={{
                                            maxWidth: "clamp(120px, 30vw, 220px)",
                                            height: "auto"
                                        }}
                                    />
                                </div>

                                <h1 className="fw-bold mb-3">
                                    Správa hobby hokejového týmu jednoduše a přehledně
                                </h1>

                                <p className="lead text-muted mb-4">
                                    Evidence zápasů, přihlášek, omluvenek, neaktivit a automatických notifikací.
                                    Podpora rolí hráčů, manažerů i administrátorů.
                                </p>

                                <div className="d-flex flex-column flex-sm-row gap-3">
                                    <Link to="/login" className="btn btn-primary btn-lg px-4 shadow-sm">
                                        Přihlásit se
                                    </Link>
                                    <Link to="/register" className="btn btn-outline-primary btn-lg px-4">
                                        Vytvořit účet
                                    </Link>
                                </div>

                                <div className="mt-5">
                                    <div className="row g-3">

                                        <div className="col-sm-6">
                                            <div className="card border-0 shadow-sm h-100">
                                                <div className="card-body">
                                                    <h6 className="fw-semibold">Zápasy</h6>
                                                    <p className="small text-muted mb-0">
                                                        Přehled nadcházejících i proběhlých zápasů.
                                                        Zobrazení nadcházejících zápasů dle typu hráče. Vytváření, změna a rušení zápasů.
                                                    </p>
                                                    <p className="small text-muted mb-0">
                                                        Detail zápasů s počty hráčů.
                                                    </p>
                                                </div>
                                            </div>
                                        </div>

                                        <div className="col-sm-6">
                                            <div className="card border-0 shadow-sm h-100">
                                                <div className="card-body">
                                                    <h6 className="fw-semibold">Registrace na zápas</h6>
                                                    <p className="small text-muted mb-0">
                                                        Účast, náhradník, omluva, automatické přepočty.
                                                    </p>
                                                </div>
                                            </div>
                                        </div>

                                        <div className="col-sm-6">
                                            <div className="card border-0 shadow-sm h-100">
                                                <div className="card-body">
                                                    <h6 className="fw-semibold">Notifikace</h6>
                                                    <p className="small text-muted mb-0">
                                                        E-mail a SMS dle uživatelského nastavení.
                                                    </p>
                                                </div>
                                            </div>
                                        </div>

                                        <div className="col-sm-6">
                                            <div className="card border-0 shadow-sm h-100">
                                                <div className="card-body">
                                                    <h6 className="fw-semibold">Správa</h6>
                                                    <p className="small text-muted mb-0">
                                                        Hráči, sezóny, uživatelé a přehledy.
                                                    </p>
                                                </div>
                                            </div>
                                        </div>

                                    </div>
                                </div>
                            </div>

                            {/* RIGHT PANEL */}
                            <div className="col-lg-5">

                                {/* DEMO CARD */}
                                <div className="card shadow border-0 mb-4">
                                    <div className="card-body">
                                        <h5 className="fw-semibold mb-3">DEMO režim</h5>

                                        <p className="small text-muted">
                                            Aplikace může běžet v demonstračním režimu.
                                            Notifikace jsou zobrazovány v modálních oknech, nejsou reálně odesílány a některé operace nejsou prováděny „na ostro“. 
                                        </p>

                                        <div className="alert alert-warning small">
                                            <strong>Důležité:</strong><br />
                                            Nepoužívej reálné e-maily, telefonní čísla ani osobní údaje.
                                            Používej testovací data nebo si vytvoř vlastní demo účet.
                                        </div>
                                    </div>
                                </div>

                                {/* ROLE CARD */}
                                <div className="card shadow border-0">
                                    <div className="card-body">
                                        <h5 className="fw-semibold mb-3">Testovací přihlášení</h5>

                                        {/* ADMIN */}
                                        <div className="mb-4 p-3 bg-light rounded">
                                            <div className="d-flex justify-content-between align-items-center mb-2">
                                                <span className="badge bg-dark">Admin</span>
                                            </div>
                                            <div className="small text-muted mb-2">
                                                Kompletní správa systému – uživatelé, hráči, sezóny, zápasy.
                                            </div>
                                            <div className="small">
                                                <strong>Email:</strong> admin@example.com<br />
                                                <strong>Heslo:</strong> administrator123
                                            </div>
                                        </div>

                                        {/* MANAGER */}
                                        <div className="mb-4 p-3 bg-light rounded">
                                            <div className="d-flex justify-content-between align-items-center mb-2">
                                                <span className="badge bg-primary">Manažer</span>
                                            </div>
                                            <div className="small text-muted mb-2">
                                                Správa hráčů a zápasů, přehledy účasti.
                                            </div>
                                            <div className="small">
                                                <strong>Email:</strong> player1@example.com<br />
                                                <strong>Heslo:</strong> Heslo123
                                            </div>
                                        </div>

                                        {/* HRÁČ */}
                                        <div className="p-3 bg-light rounded">
                                            <div className="mb-2">
                                                <span className="badge bg-secondary">Hráč</span>
                                            </div>
                                            <div className="small text-muted mb-2">
                                                Registrace na zápasy, omluvy, vlastní nastavení notifikací.
                                            </div>
                                            <div className="small">
                                                <strong>Email:</strong> player(1-10)@example.com<br />
                                                <strong>Heslo:</strong> Heslo123
                                            </div>
                                        </div>

                                        <div className="border-top pt-3 mt-3 small text-muted text-center">
                                            ⚠ Pouze pro DEMO účely – nepoužívej reálné osobní údaje.
                                        </div>
                                    </div>
                                </div>


                            </div>
                        </div>
                    </div>
                </div>
            </main>

            {/* FOOTER */}
            <footer className="bg-white border-top">
                <div className="container py-3 small text-muted d-flex justify-content-between">
                    <span>© {new Date().getFullYear()} Petr Hlista - HobbyHokejApp</span>
                    <span>DEMO / Produkční režim dle konfigurace</span>
                </div>
            </footer>
        </div>
    );
};

export default PublicLandingPage;
