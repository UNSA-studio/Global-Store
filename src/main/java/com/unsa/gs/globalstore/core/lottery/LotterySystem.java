package com.unsa.gs.globalstore.core.lottery;

import net.minecraft.world.entity.player.Player;

import java.util.*;

public class LotterySystem {
    private static final Random RANDOM = new Random();
    private static final int PITY_THRESHOLD = 10;
    // 每个玩家独立保底计数
    private static final Map<UUID, Integer> PLAYER_PITY = new HashMap<>();

    public static long roll(Player player, long betAmount) {
        UUID uuid = player.getUUID();
        int pity = PLAYER_PITY.getOrDefault(uuid, 0) + 1;
        PLAYER_PITY.put(uuid, pity);

        if (pity > PITY_THRESHOLD) {
            PLAYER_PITY.put(uuid, 0);
            return (long)(betAmount * 0.5); // 赢50%
        }

        int dice = RANDOM.nextInt(100);
        if (dice < 10) {
            PLAYER_PITY.put(uuid, 0);
            return (long)(betAmount * 2.0); // 翻倍
        } else if (dice < 30) {
            PLAYER_PITY.put(uuid, 0);
            return (long)(betAmount * 0.5); // 赢50%
        } else if (dice < 50) {
            PLAYER_PITY.put(uuid, 0);
            return betAmount; // 不赚不赔
        } else if (dice < 70) {
            return (long)(-betAmount * 0.5); // 赔50%
        } else {
            return -betAmount; // 全赔
        }
    }

    /**
     * 兼容旧调用（无player参数）。全局保底，不推荐。
     */
    @Deprecated
    public static long roll(long betAmount) {
        // 生成临时UUID，退化为无保底模式
        int dice = RANDOM.nextInt(100);
        if (dice < 10) return (long)(betAmount * 2.0);
        else if (dice < 30) return (long)(betAmount * 0.5);
        else if (dice < 50) return betAmount;
        else if (dice < 70) return (long)(-betAmount * 0.5);
        else return -betAmount;
    }
}
