package com.unsa.gs.globalstore.core.market;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;

public class MarketItemData {
    private final Item item;
    private long minPrice;
    private long maxPrice;
    private long currentBuyPrice;
    private long currentSellPrice;
    private int stock;
    private int lowStockThreshold;
    private int officialRestockAmount;
    private final LinkedList<Long> priceHistory = new LinkedList<>(); // 最近10次价格

    public MarketItemData(ItemStack stack, long min, long max) {
        this.item = stack.getItem();
        this.minPrice = min;
        this.maxPrice = max;
        this.currentBuyPrice = min;
        this.currentSellPrice = min;
        this.stock = 0;
        this.lowStockThreshold = 10;
        this.officialRestockAmount = (int)(lowStockThreshold * 1.3);
        for (int i = 0; i < 10; i++) priceHistory.add(min);
    }

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
        priceHistory.addLast(currentSellPrice);
        if (priceHistory.size() > 10) priceHistory.removeFirst();
    }

    public long getPreviousPrice() { // 昨天价格
        return priceHistory.size() > 1 ? priceHistory.get(priceHistory.size() - 2) : currentSellPrice;
    }

    public boolean isLowOnStock() { return stock < lowStockThreshold; }
}
