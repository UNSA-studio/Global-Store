package com.unsa.gs.globalstore.core.market;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RecycleHandler {
    // 返回回收价格（CC）
    public static long getRecyclePrice(Item item) {
        MarketItemData data = GlobalMarket.getData(item);
        if (data == null) return 0;
        return (long)(data.getCurrentBuyPrice() * 0.6);
    }

    // 执行回收：物品消失，玩家账户加钱
    public static long recycleItem(ItemStack stack, int amount) {
        long pricePerItem = getRecyclePrice(stack.getItem());
        return pricePerItem * amount;
    }
}
