package com.unsa.gs.globalstore.core.stock;

import net.minecraft.world.entity.player.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreditScore {
    private static final Map<UUID, Integer> SCORES = new HashMap<>();

    public static int getScore(Player player) {
        return SCORES.getOrDefault(player.getUUID(), 100); // 默认满分100
    }

    public static void setScore(Player player, int score) {
        SCORES.put(player.getUUID(), Math.min(100, Math.max(0, score)));
    }

    public static void addScore(Player player, int delta) {
        setScore(player, getScore(player) + delta);
    }

    public static boolean canTrade(Player player) {
        return getScore(player) >= 30;
    }

    public static void recoverScore(Player player, int amount) {
        // 每10分=1万CC
        int score = getScore(player);
        if (score < 30) {
            // 需要支付 (30 - score) / 10 * 10000 CC
        }
    }
}
