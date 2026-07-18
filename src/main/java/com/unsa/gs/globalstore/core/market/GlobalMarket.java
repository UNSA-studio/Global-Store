package com.unsa.gs.globalstore.core.market;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.HashMap;
import java.util.Map;

public class GlobalMarket {
    public static final Map<Item, MarketItemData> MARKET_ITEMS = new HashMap<>();

    public static void initDefaultItems() {
        // 示例：添加原版物品
        addItem(new ItemStack(Items.DIAMOND), 100, 10000);
        addItem(new ItemStack(Items.NETHERITE_INGOT), 1000, 1000000);
    }

    private static void addItem(ItemStack stack, long min, long max) {
        MarketItemData data = new MarketItemData(stack);
        // 设置min,max...
        MARKET_ITEMS.put(stack.getItem(), data);
    }

    public static MarketItemData getData(Item item) {
        return MARKET_ITEMS.get(item);
    }
}
