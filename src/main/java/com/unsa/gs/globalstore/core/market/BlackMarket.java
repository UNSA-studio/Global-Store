package com.unsa.gs.globalstore.core.market;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.*;

public class BlackMarket {
    // 黑市高价值物品候选池（仅无法合成/自然生成的，示例用少量物品）
    public static final List<ItemStack> HIGH_VALUE_POOL = Arrays.asList(
            new ItemStack(Items.NETHER_STAR),
            new ItemStack(Items.ELYTRA),
            new ItemStack(Items.TOTEM_OF_UNDYING),
            new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)
    );

    // 当天黑市商品
    public static final Map<Item, BlackMarketOffer> DAILY_OFFERS = new HashMap<>();

    // 刷新每日商品
    public static void refreshDailyOffers() {
        DAILY_OFFERS.clear();
        Random rand = new Random();
        Collections.shuffle(HIGH_VALUE_POOL);
        int count = 2 + rand.nextInt(3); // 2-4 种物品
        for (int i = 0; i < Math.min(count, HIGH_VALUE_POOL.size()); i++) {
            ItemStack stack = HIGH_VALUE_POOL.get(i).copy();
            int stock = 10 + rand.nextInt(11); // 10-20 个
            // 若是无法合成/自然生成物品（如基岩？这里仅判断是否为原版不可合成），最高3个
            if (isUnsynthesisable(stack.getItem())) {
                stock = 1 + rand.nextInt(3); // 1-3
            }
            long baseValuePC = estimateValueInPC(stack); // 估算值，稍后实现
            long pricePC = (long)(baseValuePC * 0.6); // 黑市打六折
            DAILY_OFFERS.put(stack.getItem(), new BlackMarketOffer(stack.copy(), pricePC, stock));
        }
    }

    private static boolean isUnsynthesisable(Item item) {
        // 简化：物品没有合成配方则视为不可合成
        // 实际应检查 RecipeManager，这里先根据已知物品判断
        return item == Items.NETHER_STAR || item == Items.ELYTRA || item == Items.TOTEM_OF_UNDYING;
    }

    private static long estimateValueInPC(ItemStack stack) {
        // 临时固定值，后续用估价算法代替
        if (stack.getItem() == Items.NETHER_STAR) return 100; // 100 PC
        if (stack.getItem() == Items.ELYTRA) return 50;
        if (stack.getItem() == Items.TOTEM_OF_UNDYING) return 30;
        if (stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) return 10;
        return 5;
    }

    public static class BlackMarketOffer {
        public ItemStack item;
        public long priceInPC;
        public int stock;

        public BlackMarketOffer(ItemStack item, long priceInPC, int stock) {
            this.item = item;
            this.priceInPC = priceInPC;
            this.stock = stock;
        }
    }
}
