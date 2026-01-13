package cz.phsoft.hokej.security;


import cz.phsoft.hokej.data.entities.PlayerEntity;

// pro výběr hráče
public class CurrentPlayerContext {

        private static final ThreadLocal<PlayerEntity> currentPlayer = new ThreadLocal<>();

        public static void set(PlayerEntity player) {
            currentPlayer.set(player);
        }

        public static PlayerEntity get() {
            return currentPlayer.get();
        }

        public static void clear() {
            currentPlayer.remove();
        }
    }
