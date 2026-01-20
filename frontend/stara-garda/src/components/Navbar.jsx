import React, { useState, useEffect } from "react";
import { logout, getCurrentUser } from "../api/auth";
import { GiHamburgerMenu } from "react-icons/gi";
import { AiOutlineClose } from "react-icons/ai";
import { NavLink, useLocation } from "react-router-dom";
import "./Navbar.css"

const Navbar = () => {

    const [showMenu, setShowMenu] = useState(false);
    const [fullName, setFullName] = useState("nepřihlášen"); // Výchozí stav
    const location = useLocation(); // sleduje změnu stránky (login → /)


    const closeMenu = () => {
        if (window.innerWidth < 700) setShowMenu(false);
    };

    // Funkce pro načtení aktuálního uživatele
    const loadUser = async () => {
        try {
            const user = await getCurrentUser();
            if (user?.name && user?.surname) {
                setFullName(`${user.name} ${user.surname}`);
            } else {
                setFullName("nepřihlášen");
            }
        } catch (err) {
            setFullName("nepřihlášen");
        }
    };

    // - při prvním načtení
    // - pokaždé, když se změní stránka (např. po loginu redirect na "/")
    useEffect(() => {
        const timer = setTimeout(loadUser, 300);
        return () => clearTimeout(timer);
    }, [location]);

    return (
        <nav className="navbar navbar-light bg-light">
            <div className="container">
                <span className="navbar-brand">Hokej App</span>

                {/* Hamburger */}
                <div className="nav-toggle">
                    <button
                        className="hamburger-btn"
                        onClick={() => setShowMenu((s) => !s)}
                        aria-label={showMenu ? "Zavřít menu" : "Otevřít menu"}
                    >
                        {showMenu ? (
                            <AiOutlineClose className="hamburger-icon" />
                        ) : (
                            <GiHamburgerMenu className="hamburger-icon" />
                        )}
                    </button>
                </div>

                {/* Menu */}
                <div className={`nav-list ${showMenu ? "show" : "hide"}`}>
                    <ul>
                        <li>
                            <NavLink to="/matches" className={({ isActive }) =>
                                isActive ? "activeLink" : "nonactiveLink"
                            } onClick={closeMenu}>
                                Zápasy
                            </NavLink>
                        </li>

                        <li>
                            <NavLink to="/registrations" className={({ isActive }) =>
                                isActive ? "activeLink" : "nonactiveLink"
                            } onClick={closeMenu}>
                                Registrace
                            </NavLink>
                        </li>

                        <li>
                            <NavLink to="/players" className={({ isActive }) =>
                                isActive ? "activeLink" : "nonactiveLink"
                            } onClick={closeMenu}>
                                Hráči
                            </NavLink>
                        </li>

                        <li>
                            <NavLink to="/contact" className={({ isActive }) =>
                                isActive ? "activeLink" : "nonactiveLink"
                            } onClick={closeMenu}>
                                Kontakt
                            </NavLink>
                        </li>
                    </ul>
                </div>

                {/* PRAVÁ STRANA - UŽIVATEL */}
                <div className="d-flex align-items-center gap-3">
                    <span className="navbar-text">👤 {fullName}</span>

                    <button className="btn btn-outline-danger" onClick={logout}>
                        Odhlásit
                    </button>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;