import { useAuth } from "../hooks/useAuth";
import { useImpersonation } from "../context/ImpersonationContext";

const RoleGuard = ({ roles, children }) => {
    const { user } = useAuth();
    const { info } = useImpersonation();

    if (!user) return null;

    const hasAccess =
        roles.includes(user.role) ||
        (
            user.role === "ROLE_ADMIN" &&
            info?.impersonating &&
            roles.includes("ROLE_PLAYER")
        );

    if (!hasAccess) return null;

    return children;
};

export default RoleGuard;
