import { Outlet } from "react-router-dom";
import HeaderTop from "../components/HeaderTop";

/**
 * PublicLayout
 *
 * UI komponenta.
 *
 * @param {Object} props vstupní hodnoty komponenty
 */
const PublicLayout = () => {
    return (
        <>
            <HeaderTop />
            <Outlet />
        </>
    );
};

export default PublicLayout;
