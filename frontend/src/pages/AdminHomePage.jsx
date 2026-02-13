// src/pages/AdminHomePage.jsx

const AdminHomePage = () => {
    return (
        <div className="container mt-4">
            <h1 className="h3 mb-3">Admin – domovská stránka</h1>

            <p className="text-muted">
                Tato stránka je viditelná pouze pro administrátory.
                Obsah můžeme později doplnit například o:
            </p>

            <ul>
                <li>rychlé odkazy na správu sezón, zápasů, uživatelů</li>
                <li>souhrn nadcházejících zápasů</li>
                <li>statistiky (počty uživatelů, hráčů, sezón…)</li>
                <li>poslední aktivity / změny v systému</li>
            </ul>

            <p className="text-muted">
                Zatím je to placeholder  jakmile budeš mít konkrétní požadavky,
                doplníme boxy, grafy a další informace.
            </p>
        </div>
    );
};

export default AdminHomePage;
