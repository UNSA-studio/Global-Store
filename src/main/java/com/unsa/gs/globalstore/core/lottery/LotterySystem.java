package com.unsa.gs.globalstore.core.lottery;

import java.util.Random;

public class LotterySystem {
    private static final Random RANDOM = new Random();
    // 保底计数（每个玩家独立，这里简化为全局隐藏值，实际应用需绑玩家数据）
    private static int pityCounter = 0;
    private static final int PITY_THRESHOLD = 10; // 10次没赚钱强制触发小奖

    // 结果：正数赚钱，负数赔钱，0不变
    public static long roll(long betAmount) {
        pityCounter++;
        // 隐藏保底：超过阈值且一直没赚，给固定小奖
        if (pityCounter > PITY_THRESHOLD) {
            pityCounter = 0;
            return (long)(betAmount * 0.5); // 赢50%
        }

        int dice = RANDOM.nextInt(100);
        if (dice < 10) {
            pityCounter = 0;
            return (long)(betAmount * 2.0); // 翻倍
        } else if (dice < 30) {
            pityCounter = 0;
            return (long)(betAmount * 0.5); // 赢50%
        } else if (dice < 50) {
            pityCounter = 0;
            return betAmount; // 不赚不赔
        } else if (dice < 70) {
            return (long)(-betAmount * 0.5); // 赔50%
        } else {
            return -betAmount; // 全赔
        }
    }
}
