package com.unsa.gs.globalstore.core.stock;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 信用分系统。
 * 默认 100 分，低于 30 分禁止交易。
 * 操作入口迁移到 GUI 银行标签页。
 */
public class CreditScore {
    private static final Map<UUID, Integer> SCORES = new HashMap<>();

    public static int getScore(Player player) {
        return SCORES.getOrDefault(player.getUUID(), 100);
    }

    public static void setScore(Player player, int score) {
        SCORES.put(player.getUUID(), Math.min(100, Math.max(0, score)));
    }

    public static void addScore(Player player, int delta) {
        setScore(player, getScore(player) + delta);
    }

    /**
     * 信用分不足，禁止交易。
     */
    public static boolean canTrade(Player player) {
        return getScore(player) >= 30;
    }

    /**
     * 花钱恢复信用分 — 从 GUI 调用。
     * @return 是否成功
     */
    public static boolean recoverScore(Player player, int points) {
        if (points <= 0 || points > 100) return false;
        long cost = points * 1000L; // 1点 = 1000 CC
        var account = player.getData(
            com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        if (account.getBalance() < cost) return false;
        account.subtractBalance(cost);
        addScore(player, points);
        return true;
    }
}
