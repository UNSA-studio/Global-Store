package com.unsa.gs.globalstore.core.market;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MarketItemData {
    private Item item;
    private long minPrice;
    private long maxPrice;
    private long currentBuyPrice;  // 市场从玩家买入价格（玩家卖）
    private long currentSellPrice; // 市场卖给玩家价格
    private int stock;              // 当前库存
    private int lowStockThreshold;  // 低于此值官方补货
    private int officialRestockAmount; // 补货数量

    public MarketItemData(ItemStack stack, long min, long max) {
        this.item = stack.getItem();
        this.minPrice = min;
        this.maxPrice = max;
        this.currentBuyPrice = min;
        this.currentSellPrice = max;
        this.stock = 0;
        this.lowStockThreshold = 10;
        this.officialRestockAmount = (int)(lowStockThreshold * 1.3); // 超过阈值30%
    }

    // Getters and setters...
    public Item getItem() { return item; }
    public long getMinPrice() { return minPrice; }
    public long getMaxPrice() { return maxPrice; }
    public long getCurrentBuyPrice() { return currentBuyPrice; }
    public long getCurrentSellPrice() { return currentSellPrice; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getLowStockThreshold() { return lowStockThreshold; }

    public void updatePrices(long buy, long sell) {
        this.currentBuyPrice = Math.max(minPrice, Math.min(maxPrice, buy));
        this.currentSellPrice = Math.max(minPrice, Math.min(maxPrice, sell));
    }

    public boolean isLowOnStock() {
        return stock < lowStockThreshold;
    }
}
