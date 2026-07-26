package com.unsa.gs.globalstore.core.market;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.HashMap;
import java.util.Map;

public class GlobalMarket {
    public static final Map<Item, MarketItemData> MARKET_ITEMS = new HashMap<>();

    public static void initDefaultItems() {
        addItem(new ItemStack(Items.DIAMOND), 50, 200, 32);
        addItem(new ItemStack(Items.EMERALD), 5, 30, 64);
        addItem(new ItemStack(Items.IRON_INGOT), 2, 10, 128);
        addItem(new ItemStack(Items.GOLD_INGOT), 5, 20, 64);
        addItem(new ItemStack(Items.NETHERITE_INGOT), 500, 2000, 8);
        addItem(new ItemStack(Items.ENDER_PEARL), 5, 20, 32);
        addItem(new ItemStack(Items.BLAZE_ROD), 10, 40, 16);
        addItem(new ItemStack(Items.SLIME_BALL), 3, 15, 48);
        addItem(new ItemStack(Items.OBSIDIAN), 1, 8, 128);
        addItem(new ItemStack(Items.ENCHANTING_TABLE), 20, 80, 4);
        addItem(new ItemStack(Items.ANVIL), 30, 100, 4);
        addItem(new ItemStack(Items.ENCHANTED_BOOK), 10, 50, 1);
        addItem(new ItemStack(Items.BOOK), 1, 5, 32);
        addItem(new ItemStack(Items.COAL), 1, 3, 256);
        addItem(new ItemStack(Items.REDSTONE), 1, 4, 256);
        addItem(new ItemStack(Items.LAPIS_LAZULI), 1, 5, 128);
        addItem(new ItemStack(Items.QUARTZ), 2, 8, 64);
    }

    private static void addItem(ItemStack stack, long min, long max, int initialStock) {
        MarketItemData data = new MarketItemData(stack, min, max);
        data.setStock(initialStock);
        MARKET_ITEMS.put(stack.getItem(), data);
    }

    public static MarketItemData getData(Item item) { return MARKET_ITEMS.get(item); }

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
