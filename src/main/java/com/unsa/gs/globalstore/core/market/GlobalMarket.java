package com.unsa.gs.globalstore.core.market;

import com.unsa.gs.globalstore.core.market.recipe.RecipeAnalyzer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * 全球市场 — 全物品支持。
 * 原版物品使用固定价格范围，模组物品通过配方分析自动估值。
 * 只允许可合成或自然生成的物品交易。
 */
public class GlobalMarket {
    public static final Map<Item, MarketItemData> MARKET_ITEMS = new LinkedHashMap<>();

    /**
     * 初始化：扫描所有已注册物品，分类加入市场或黑市。
     * 在服务端启动时调用。
     */
    public static void initAllItems() {
        MARKET_ITEMS.clear();

        // 缓存：预建可合成物品集合，避免每个物品都遍历所有配方
        Set<Item> craftableItems = new HashSet<>();
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            Level level = server.overworld();
            if (level != null) {
                for (var holder : level.getRecipeManager().getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING)) {
                    ItemStack result = holder.value().getResultItem(level.registryAccess());
                    if (result.getItem() != Items.AIR) {
                        craftableItems.add(result.getItem());
                    }
                }
            }
        }

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;

            // 跳过黑市专属物品（不可正常获取的）
            if (RecipeAnalyzer.isUnobtainable(item)) continue;
            // 跳过硬币本身
            if (isCoinItem(item)) continue;

            // 原版物品：使用固定价格范围
            long[] vanillaRange = RecipeAnalyzer.getVanillaPriceRange(item);
            if (vanillaRange != null) {
                addItem(new ItemStack(item), vanillaRange[0], vanillaRange[1],
                    calcInitialStock(item, vanillaRange[0]), false);
                continue;
            }

            // 模组物品：检查是否有合成配方
            if (RecipeAnalyzer.isVanilla(item)) {
                // 原版物品但没有预设价格：用缓存查询
                if (craftableItems.contains(item)) {
                    long[] range = new long[]{5, 20};
                    addItem(new ItemStack(item), range[0], range[1], 32, false);
                }
                continue;
            }

            // 模组物品
            if (craftableItems.contains(item)) {
                long baseCost = RecipeAnalyzer.analyzeItemCost(item, null, 6);
                long[] range;
                if (baseCost > 0) {
                    range = RecipeAnalyzer.generateModItemPriceRange(baseCost);
                } else {
                    range = new long[]{10, 50}; // 兜底
                }
                addItem(new ItemStack(item), range[0], range[1],
                    calcInitialStock(item, range[0]), true);
            }
            // 不能合成且不是原版的模组物品：不给市场入口
        }
    }

    /**
     * @deprecated 使用 initAllItems() 代替
     */
    @Deprecated
    public static void initDefaultItems() {
        // 保留旧逻辑以免破坏引用
        addItem(new ItemStack(Items.DIAMOND), 50, 200, 32, false);
        addItem(new ItemStack(Items.EMERALD), 5, 30, 64, false);
        addItem(new ItemStack(Items.IRON_INGOT), 2, 10, 128, false);
        addItem(new ItemStack(Items.GOLD_INGOT), 5, 20, 64, false);
        addItem(new ItemStack(Items.NETHERITE_INGOT), 500, 2000, 8, false);
        addItem(new ItemStack(Items.ENDER_PEARL), 5, 20, 32, false);
        addItem(new ItemStack(Items.BLAZE_ROD), 10, 40, 16, false);
        addItem(new ItemStack(Items.SLIME_BALL), 3, 15, 48, false);
        addItem(new ItemStack(Items.OBSIDIAN), 1, 8, 128, false);
        addItem(new ItemStack(Items.ENCHANTING_TABLE), 20, 80, 4, false);
        addItem(new ItemStack(Items.ANVIL), 30, 100, 4, false);
        addItem(new ItemStack(Items.ENCHANTED_BOOK), 10, 50, 1, false);
        addItem(new ItemStack(Items.BOOK), 1, 5, 32, false);
        addItem(new ItemStack(Items.COAL), 1, 3, 256, false);
        addItem(new ItemStack(Items.REDSTONE), 1, 4, 256, false);
        addItem(new ItemStack(Items.LAPIS_LAZULI), 1, 5, 128, false);
        addItem(new ItemStack(Items.QUARTZ), 2, 8, 64, false);
    }

    private static void addItem(ItemStack stack, long min, long max, int initialStock, boolean isModded) {
        if (MARKET_ITEMS.containsKey(stack.getItem())) return;
        MarketItemData data = new MarketItemData(stack, min, max, isModded);
        data.setStock(initialStock);
        MARKET_ITEMS.put(stack.getItem(), data);
    }

    private static int calcInitialStock(Item item, long minPrice) {
        // 根据价格决定初始库存
        if (minPrice >= 500) return 8;
        if (minPrice >= 100) return 16;
        if (minPrice >= 10) return 64;
        return 128;
    }

    private static boolean isCoinItem(Item item) {
        var key = BuiltInRegistries.ITEM.getKey(item);
        if (key == null) return false;
        return key.getNamespace().equals("globalstore") &&
            (key.getPath().contains("coin") || key.getPath().contains("currency"));
    }

    public static MarketItemData getData(Item item) { return MARKET_ITEMS.get(item); }

    /**
     * 购买物品（玩家从市场买入）。
     */
    public static boolean buyItem(Item item, int amount) {
        MarketItemData data = getData(item);
        if (data == null || data.getStock() < amount) return false;
        data.setStock(data.getStock() - amount);
        data.recordTrade(true, amount);
        return true;
    }

    /**
     * 卖出物品（玩家向市场出售）。
     */
    public static boolean sellItem(Item item, int amount) {
        MarketItemData data = getData(item);
        if (data == null) return false;
        data.setStock(data.getStock() + amount);
        data.recordTrade(false, amount);
        return true;
    }

    public static void restockIfNeeded() {
        for (MarketItemData data : MARKET_ITEMS.values()) {
            if (data.isLowOnStock()) {
                data.setStock(data.getStock() + data.getLowStockThreshold()
                    + (int)(data.getLowStockThreshold() * 0.3));
                data.resetDailyVolume();
            }
        }
    }
}
