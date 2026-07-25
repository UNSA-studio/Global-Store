package com.unsa.gs.globalstore.core.market;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.HashMap;
import java.util.Map;

public class GlobalMarket {
    public static final Map<Item, MarketItemData> MARKET_ITEMS = new HashMap<>();

    public static void initDefaultItems() {
        // 临时添加几个原版物品用于测试
        addItem(new ItemStack(Items.DIAMOND), 100, 10000);
        addItem(new ItemStack(Items.NETHERITE_INGOT), 1000, 1000000);
        addItem(new ItemStack(Items.EMERALD), 10, 500);
    }

    private static void addItem(ItemStack stack, long min, long max) {
        MARKET_ITEMS.put(stack.getItem(), new MarketItemData(stack, min, max));
    }

    public static MarketItemData getData(Item item) {
        return MARKET_ITEMS.get(item);
    }

    // 模拟交易：玩家购买物品，减少库存，价格微调
    public static boolean buyItem(Item item, int amount, long totalCost) {
        MarketItemData data = getData(item);
        if (data == null || data.getStock() < amount) return false;
        data.setStock(data.getStock() - amount);
        // 价格略微上涨（简化）
        long newBuy = data.getCurrentBuyPrice() + 5;
        long newSell = data.getCurrentSellPrice() + 10;
        data.updatePrices(newBuy, newSell);
        return true;
    }

    // 玩家卖物品给市场
    public static boolean sellItem(Item item, int amount) {
        MarketItemData data = getData(item);
        if (data == null) return false;
        data.setStock(data.getStock() + amount);
        // 价格下跌
        long newBuy = data.getCurrentBuyPrice() - 5;
        long newSell = data.getCurrentSellPrice() - 10;
        data.updatePrices(newBuy, newSell);
        return true;
    }

    // 补货检查（每天调用一次）
    public static void restockIfNeeded() {
        for (MarketItemData data : MARKET_ITEMS.values()) {
            if (data.isLowOnStock()) {
                data.setStock(data.getStock() + data.getLowStockThreshold() + (int)(data.getLowStockThreshold()*0.3));
            }
        }
    }
}
