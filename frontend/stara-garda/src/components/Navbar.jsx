// UI navbar bez API logiky, UI čisté, logika logout v API vrstvě.

import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { GiHamburgerMenu } from "react-icons/gi";
import { AiOutlineClose } from "react-icons/ai";
import { logoutUser } from "../api/authApi";
import { useAuth } from "../hooks/useAuth";
import { PlayerIcon } from "../icons";
import { UserIcon } from "../icons";
import { useCurrentPlayer } from "../hooks/useCurrentPlayer";

import "./Navbar.css";

const Navbar = () => {
    const [showMenu, setShowMenu] = useState(false);
    const { user, logout } = useAuth();
    const { currentPlayer } = useCurrentPlayer()
    const navigate = useNavigate();

    const closeMenu = () => {
        if (window.innerWidth < 700) {
            setShowMenu(false);
        }
    };

    const handleLogout = async () => {
        await logout();
        navigate("/login");
    };

    return (
        <nav className="navbar navbar-light bg-light">
            <div className="container">
                <span className="navbar-brand">Hokej App</span>

                <button
                    className="nav-toggle"
                    onClick={() => setShowMenu(!showMenu)}
                    aria-label="Toggle menu"
                >
                    {showMenu ? <AiOutlineClose /> : <GiHamburgerMenu />}
                </button>

                <div className={`nav-list ${showMenu ? "show" : "hide"}`}>
                    <ul>
                        <li>
                            <NavLink to="/players" className={({ isActive }) =>
                                isActive ? "activeLink" : "nonactiveLink"
                            } onClick={closeMenu}>
                                Hráči
                            </NavLink>
                        </li>

                        <li>
                            <NavLink to="/Matches" className={({ isActive }) =>
                                isActive ? "activeLink" : "nonactiveLink"
                            } onClick={closeMenu}>
                                Zápasy
                            </NavLink>
                        </li>

                        <li>
                            <NavLink to="/Registrace" className={({ isActive }) =>
                                isActive ? "activeLink" : "nonactiveLink"
                            } onClick={closeMenu}>
                                Registrace
                            </NavLink>
                        </li>

                        <li>
                            <NavLink to="/contact" className={({ isActive }) =>
                                isActive ? "activeLink" : "nonactiveLink"
                            } onClick={closeMenu}>
                                Nastavení
                            </NavLink>

                        </li>
                    </ul>
                </div>
                <div className="d-flex gap-3">
                    {user && (
                        <>
                            <span>
                                <UserIcon />{" "}
                                {user.name} {user.surname}
                                <div><PlayerIcon />{" "}  {currentPlayer
                                    ? `${currentPlayer.name} ${currentPlayer.surname}`
                                    : "Není vybrán hráč"}</div>
                            </span>

                            <button className="btn btn-outline-danger" onClick={handleLogout}>
                                Odhlásit
                            </button>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;