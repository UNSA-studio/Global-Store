package com.unsa.gs.globalstore.core.market;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.HashMap;
import java.util.Map;

public class GlobalMarket {
    public static final Map<Item, MarketItemData> MARKET_ITEMS = new HashMap<>();

    public static void initDefaultItems() {
        // 合理定价：物品 | 最低买入价(玩家卖) | 最高卖出价(市场卖)
        addItem(new ItemStack(Items.DIAMOND), 50, 200);
        addItem(new ItemStack(Items.EMERALD), 5, 30);
        addItem(new ItemStack(Items.IRON_INGOT), 2, 10);
        addItem(new ItemStack(Items.GOLD_INGOT), 5, 20);
        addItem(new ItemStack(Items.NETHERITE_INGOT), 500, 2000);
        addItem(new ItemStack(Items.ENDER_PEARL), 5, 20);
        addItem(new ItemStack(Items.BLAZE_ROD), 10, 40);
        addItem(new ItemStack(Items.SLIME_BALL), 3, 15);
    }

    private static void addItem(ItemStack stack, long min, long max) {
        MARKET_ITEMS.put(stack.getItem(), new MarketItemData(stack, min, max));
    }

    public static MarketItemData getData(Item item) {
        return MARKET_ITEMS.get(item);
    }

    public static boolean buyItem(Item item, int amount, long totalCost) {
        MarketItemData data = getData(item);
        if (data == null || data.getStock() < amount) return false;
        data.setStock(data.getStock() - amount);
        long newBuy = data.getCurrentBuyPrice() + 1;
        long newSell = data.getCurrentSellPrice() + 2;
        data.updatePrices(newBuy, newSell);
        return true;
    }

    public static boolean sellItem(Item item, int amount) {
        MarketItemData data = getData(item);
        if (data == null) return false;
        data.setStock(data.getStock() + amount);
        long newBuy = data.getCurrentBuyPrice() - 1;
        long newSell = data.getCurrentSellPrice() - 2;
        data.updatePrices(newBuy, newSell);
        return true;
    }

    public static void restockIfNeeded() {
        for (MarketItemData data : MARKET_ITEMS.values()) {
            if (data.isLowOnStock()) {
                data.setStock(data.getStock() + data.getLowStockThreshold() + (int)(data.getLowStockThreshold()*0.3));
            }
        }
    }
}
