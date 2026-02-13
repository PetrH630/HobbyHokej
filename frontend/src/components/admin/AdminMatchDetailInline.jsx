// src/components/admin/AdminMatchDetailInline.jsx
import MatchHeader from "../matches/MatchHeader";
import AdminMatchInfo from "../admin/AdminMatchInfo";
import { useMatchDetail } from "../../hooks/useMatchDetail";

const AdminMatchDetailInline = ({ matchId }) => {
    const { match, loading, error, reload } = useMatchDetail(matchId);

    if (loading) {
        return <p>Načítám detail zápasu…</p>;
    }

    if (error) {
        return <p className="text-danger">{error}</p>;
    }

    if (!match) {
        return <p>Detail zápasu nebyl nalezen.</p>;
    }

    return (
        <div>            
            <AdminMatchInfo match={match} onRefresh={reload}/>
        </div>
    );
};

export default AdminMatchDetailInline;
