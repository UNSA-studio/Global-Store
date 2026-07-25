package com.unsa.gs.globalstore.core.stock;

import java.util.Random;

public class StockCompany {
    public final String name;
    public final boolean isRealWorld; // 是否真实公司
    private long currentPrice;        // 股价（CC）
    private long volatility;          // 波动范围
    private long totalShares;
    private long availableShares;

    public StockCompany(String name, boolean isRealWorld, long initialPrice, long totalShares) {
        this.name = name;
        this.isRealWorld = isRealWorld;
        this.currentPrice = initialPrice;
        this.volatility = initialPrice / 10;
        this.totalShares = totalShares;
        this.availableShares = totalShares;
    }

    public long getPrice() { return currentPrice; }
    public long getAvailableShares() { return availableShares; }

    public void adjustPrice(long delta) {
        this.currentPrice = Math.max(1, currentPrice + delta);
        this.volatility = currentPrice / 10;
    }

    // 模拟每次 tick 的小幅波动（人机影响）
    public void tick() {
        Random rand = new Random();
        if (rand.nextInt(100) < 30) { // 30% 概率波动
            long change = (long)(rand.nextGaussian() * volatility);
            adjustPrice(change);
        }
    }
}
