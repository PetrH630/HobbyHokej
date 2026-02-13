package cz.phsoft.hokej.security.impersonation;

public final class ImpersonationContext {

    private static final ThreadLocal<Long> IMPERSONATED_PLAYER_ID = new ThreadLocal<>();

    private ImpersonationContext() {
    }

    public static void setImpersonatedPlayerId(Long playerId) {
        IMPERSONATED_PLAYER_ID.set(playerId);
    }

    public static Long getImpersonatedPlayerId() {
        return IMPERSONATED_PLAYER_ID.get();
    }

    public static boolean isImpersonating() {
        return getImpersonatedPlayerId() != null;
    }

    public static void clear() {
        IMPERSONATED_PLAYER_ID.remove();
    }
}
