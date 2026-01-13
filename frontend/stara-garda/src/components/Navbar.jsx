import React from "react";
import { logout } from "../api/auth";
import { GiHamburgerMenu } from "react-icons/gi";
import { AiOutlineClose } from "react-icons/ai";
import { useState, useEffect } from "react";
import { NavLink } from "react-router-dom";

const Navbar = () => {

    const [showMenu, setShowMenu] = useState(false);

    const closeMenu = () => {
        if (window.innerWidth < 700) setShowMenu(false);
    };

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
                            <NavLink
                                to="/"
                                className={({ isActive }) =>
                                    isActive ? "activeLink" : "nonactiveLink"
                                }
                                onClick={closeMenu}
                            >
                                Domů
                            </NavLink>
                        </li>

                        <li>
                            <NavLink
                                to="/matches"
                                className={({ isActive }) =>
                                    isActive ? "activeLink" : "nonactiveLink"
                                }
                                onClick={closeMenu}
                            >
                                Zápasy
                            </NavLink>
                        </li>

                        <li>
                            <NavLink
                                to="/registrations"
                                className={({ isActive }) =>
                                    isActive ? "activeLink" : "nonactiveLink"
                                }
                                onClick={closeMenu}
                            >
                                Registrace
                            </NavLink>
                        </li>
                        <li>
                            <NavLink
                                to="/players"
                                className={({ isActive }) =>
                                    isActive ? "activeLink" : "nonactiveLink"
                                }
                                onClick={closeMenu}
                            >
                                Hráči
                            </NavLink>
                        </li>
                        <li>
                            <NavLink
                                to="/contact"
                                className={({ isActive }) =>
                                    isActive ? "activeLink" : "nonactiveLink"
                                }
                                onClick={closeMenu}
                            >
                                Kontakt
                            </NavLink>
                        </li>
                    </ul>
                </div>
                
                
                
                <button className="btn btn-outline-danger" onClick={logout}>
                    Odhlásit
                </button>
            </div>
        </nav>
    );
};

export default Navbar;