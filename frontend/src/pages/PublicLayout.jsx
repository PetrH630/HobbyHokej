import { Outlet } from "react-router-dom";
import HeaderTop from "../components/HeaderTop";

const PublicLayout = () => {
    return (
        <>
            <HeaderTop />
            <Outlet />
        </>
    );
};

export default PublicLayout;
