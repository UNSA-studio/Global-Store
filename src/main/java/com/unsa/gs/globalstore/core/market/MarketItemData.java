package com.unsa.gs.globalstore.core.market;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;

public class MarketItemData {
    private final Item item;
    private final long minPrice;
    private final long maxPrice;
    private final boolean isModded;
    private long currentBuyPrice;
    private long currentSellPrice;
    private int stock;
    private int lowStockThreshold;
    private int dailyVolume; // 当日交易量
    private long totalVolumeSinceLastChange; // 自上次价格变动以来的累计交易量
    private final LinkedList<Long> priceHistory = new LinkedList<>();

    // 价格波动阈值：累计交易量超过此值才触发价格变化
    private static final long VOLUME_THRESHOLD_FOR_PRICE_CHANGE = 16;

    public MarketItemData(ItemStack stack, long min, long max, boolean isModded) {
        this.item = stack.getItem();
        this.minPrice = min;
        this.maxPrice = max;
        this.isModded = isModded;
        this.currentBuyPrice = min;
        this.currentSellPrice = min;
        this.stock = 0;
        this.lowStockThreshold = 10;
        this.dailyVolume = 0;
        this.totalVolumeSinceLastChange = 0;
        // 模拟历史价格：在 min 附近小范围随机波动，让涨跌箭头正常
        java.util.Random rng = new java.util.Random(stack.getItem().hashCode());
        for (int i = 0; i < 10; i++) {
            long variation = (long)(rng.nextGaussian() * Math.max(1, (max - min) * 0.1));
            priceHistory.add(Math.max(min, Math.min(max, min + variation)));
        }
    }

    // === 兼容旧构造 ===
    public MarketItemData(ItemStack stack, long min, long max) {
        this(stack, min, max, false);
    }

    // === Getters ===
    public Item getItem() { return item; }
    public long getMinPrice() { return minPrice; }
    public long getMaxPrice() { return maxPrice; }
    public long getCurrentBuyPrice() { return currentBuyPrice; }
    public long getCurrentSellPrice() { return currentSellPrice; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public boolean isModded() { return isModded; }
    public int getDailyVolume() { return dailyVolume; }

    /**
     * 记录一次交易，仅在累计量超过阈值时才更新价格。
     */
    public void recordTrade(boolean isBuy, int amount) {
        this.dailyVolume += amount;
        this.totalVolumeSinceLastChange += amount;

        if (this.totalVolumeSinceLastChange >= VOLUME_THRESHOLD_FOR_PRICE_CHANGE) {
            applyPriceChange(isBuy, this.totalVolumeSinceLastChange);
            this.totalVolumeSinceLastChange = 0;
        }
    }

    private void applyPriceChange(boolean isBuy, long volume) {
        // 价格变动幅度基于交易量
        long change = Math.max(1, volume / 4); // 每4个物品变动1 CC
        if (isBuy) {
            this.currentBuyPrice = Math.min(maxPrice, currentBuyPrice + change);
            this.currentSellPrice = Math.min(maxPrice, currentSellPrice + change);
        } else {
            this.currentBuyPrice = Math.max(minPrice, currentBuyPrice - change);
            this.currentSellPrice = Math.max(minPrice, currentSellPrice - change);
        }
        priceHistory.addLast(currentSellPrice);
        if (priceHistory.size() > 10) priceHistory.removeFirst();
    }

    /**
     * @deprecated 使用 recordTrade 代替
     */
    @Deprecated
    public void updatePrices(long buy, long sell) {
        this.currentBuyPrice = Math.max(minPrice, Math.min(maxPrice, buy));
        this.currentSellPrice = Math.max(minPrice, Math.min(maxPrice, sell));
        priceHistory.addLast(currentSellPrice);
        if (priceHistory.size() > 10) priceHistory.removeFirst();
    }

    public long getPreviousPrice() {
        return priceHistory.size() > 1 ? priceHistory.get(priceHistory.size() - 2) : currentSellPrice;
    }

    public boolean isLowOnStock() { return stock < lowStockThreshold; }

    public void resetDailyVolume() { this.dailyVolume = 0; }
}
