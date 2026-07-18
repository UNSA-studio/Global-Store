package com.unsa.gs.globalstore.core.market;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class MarketItemData {
    public static final long DEFAULT_MIN_PRICE = 1;
    public static final long DEFAULT_MAX_PRICE = 1000000;

    private final ItemStack item;
    private long minPrice;
    private long maxPrice;
    private long currentBuyPrice;   // 当前市场买入价（玩家卖给市场）
    private long currentSellPrice;  // 当前市场卖出价（市场卖给玩家）
    private int stock;              // 当前库存
    private int lowStockThreshold;  // 低于此值官方补货

    public MarketItemData(ItemStack item) {
        this.item = item;
        this.minPrice = DEFAULT_MIN_PRICE;
        this.maxPrice = DEFAULT_MAX_PRICE;
        this.currentBuyPrice = minPrice;
        this.currentSellPrice = minPrice;
        this.stock = 0;
        this.lowStockThreshold = 10; // 示例
    }

    // Getter/Setter 省略，实际需添加
    public ItemStack getItem() { return item; }
    public long getCurrentBuyPrice() { return currentBuyPrice; }
    public long getCurrentSellPrice() { return currentSellPrice; }
}
