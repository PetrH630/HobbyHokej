import { Outlet } from "react-router-dom"
import Navbar from "../components/Navbar"
import Footer from "../components/Footer"
import HeaderTop from "../components/HeaderTop"

const SharedLayout = () => {
    return (
        <div className="layout">
            <HeaderTop />
            <Navbar />
            <main className="content">
                <div className="container">
                    <Outlet />
                </div>
            </main>
            <Footer />
        </div>
    )
}

export default SharedLayout