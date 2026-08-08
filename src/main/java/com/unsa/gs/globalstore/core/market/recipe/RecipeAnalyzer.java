package com.unsa.gs.globalstore.core.market.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

/**
 * 配方分析器 — 递归分析物品的合成配方树，
 * 计算模组物品的最低价格估值，并生成合理的价格范围。
 */
public class RecipeAnalyzer {

    // 原版物品固定价格范围 (min, max)
    private static final Map<Item, long[]> VANILLA_PRICES = new HashMap<>();

    // 原版物品中无法通过合成获取的（用于黑市识别）
    private static final Set<Item> UNOBTAINABLE_VANILLA = new HashSet<>();

    static {
        // === 矿物/资源类 ===
        price("diamond", 50, 200);           // 钻石
        price("emerald", 5, 30);             // 绿宝石
        price("iron_ingot", 2, 10);          // 铁锭
        price("gold_ingot", 5, 20);          // 金锭
        price("netherite_ingot", 500, 2000); // 下界合金锭
        price("copper_ingot", 1, 5);         // 铜锭
        price("coal", 1, 3);                 // 煤炭
        price("redstone", 1, 4);             // 红石
        price("lapis_lazuli", 1, 5);         // 青金石
        price("quartz", 2, 8);               // 下界石英
        price("amethyst_shard", 5, 15);      // 紫水晶碎片
        price("raw_iron", 1, 4);             // 粗铁
        price("raw_gold", 2, 8);             // 粗金
        price("raw_copper", 1, 3);           // 粗铜
        price("netherite_scrap", 200, 800);  // 下界合金碎片

        // === 掉落物 ===
        price("ender_pearl", 5, 20);         // 末影珍珠
        price("blaze_rod", 10, 40);          // 烈焰棒
        price("slime_ball", 3, 15);          // 粘液球
        price("gunpowder", 2, 8);            // 火药
        price("string", 1, 3);               // 线
        price("bone", 1, 3);                 // 骨头
        price("spider_eye", 2, 6);           // 蜘蛛眼
        price("rotten_flesh", 1, 2);         // 腐肉
        price("prismarine_shard", 2, 8);     // 海晶碎片
        price("prismarine_crystals", 5, 15); // 海晶砂粒
        price("phantom_membrane", 10, 30);   // 幻翼膜
        price("rabbit_hide", 1, 4);          // 兔子皮
        price("rabbit_foot", 15, 50);        // 兔子脚
        price("ghast_tear", 30, 100);        // 恶魂之泪
        price("magma_cream", 5, 20);         // 岩浆膏
        price("blaze_powder", 5, 20);        // 烈焰粉
        price("shulker_shell", 50, 150);     // 潜影壳
        price("wither_skeleton_skull", 100, 500); // 凋零骷髅头颅

        // === 基础方块 ===
        price("obsidian", 1, 8);             // 黑曜石
        price("crying_obsidian", 5, 15);     // 哭泣的黑曜石
        price("glowstone_dust", 2, 6);       // 荧石粉
        price("snowball", 1, 2);             // 雪球
        price("clay_ball", 1, 3);            // 粘土球
        price("brick", 2, 6);                // 砖块
        price("nether_brick", 2, 6);         // 下界砖
        price("popped_chorus_fruit", 3, 10); // 爆裂紫颂果
        price("leather", 1, 4);              // 皮革
        price("feather", 1, 3);              // 羽毛
        price("ink_sac", 2, 6);              // 墨囊
        price("honeycomb", 3, 10);           // 蜜脾
        price("honey_bottle", 5, 15);        // 蜂蜜瓶

        // === 作物/食物 ===
        price("wheat", 1, 3);
        price("carrot", 1, 3);
        price("potato", 1, 3);
        price("beetroot", 1, 3);
        price("apple", 1, 4);
        price("golden_apple", 50, 200);
        price("sweet_berries", 1, 4);
        price("glow_berries", 2, 6);
        price("chorus_fruit", 3, 10);
        price("cocoa_beans", 2, 5);
        price("sugar_cane", 1, 2);
        price("bamboo", 1, 2);
        price("kelp", 1, 2);
        price("egg", 1, 3);
        price("pumpkin", 2, 8);
        price("melon_slice", 1, 3);
        price("cactus", 1, 3);
        price("vine", 1, 2);

        // === 合成品（有配方的成品，价格基于原材料累加）===
        // 这类物品通过配方分析自动计算，这里设置的是兜底价格
        price("enchanting_table", 20, 80);
        price("anvil", 30, 100);
        price("enchanted_book", 10, 50);
        price("book", 1, 5);
        price("beacon", 500, 5000);
        price("conduit", 200, 800);
        price("ender_chest", 20, 60);
        price("jukebox", 15, 40);

        // === 染料 ===
        for (String dye : new String[]{"white","orange","magenta","light_blue","yellow",
            "lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black"}) {
            price(dye + "_dye", 1, 4);
        }

        // === 无法正常获取的原版物品（黑市专属）===
        unobtainable(Items.BEDROCK);
        unobtainable(Items.BARRIER);
        unobtainable(Items.COMMAND_BLOCK);
        unobtainable(Items.CHAIN_COMMAND_BLOCK);
        unobtainable(Items.REPEATING_COMMAND_BLOCK);
        unobtainable(Items.STRUCTURE_BLOCK);
        unobtainable(Items.STRUCTURE_VOID);
        unobtainable(Items.JIGSAW);
        unobtainable(Items.LIGHT);
        unobtainable(Items.KNOWLEDGE_BOOK);
        unobtainable(Items.DEBUG_STICK);
        unobtainable(Items.END_PORTAL_FRAME);
        unobtainable(Items.DRAGON_EGG);
        unobtainable(Items.ELYTRA);
        unobtainable(Items.ENCHANTED_GOLDEN_APPLE);
        unobtainable(Items.NETHER_STAR);
        unobtainable(Items.TOTEM_OF_UNDYING);
        unobtainable(Items.HEART_OF_THE_SEA);
        unobtainable(Items.TRIDENT);
        unobtainable(Items.ECHO_SHARD);
        unobtainable(Items.DISC_FRAGMENT_5);
        unobtainable(Items.REINFORCED_DEEPSLATE);
        unobtainable(Items.SPAWNER);
        unobtainable(Items.BUDDING_AMETHYST);
    }

    private static void price(String itemName, long min, long max) {
        Item item = BuiltInRegistries.ITEM.getOptional(
            net.minecraft.resources.ResourceLocation.withDefaultNamespace(itemName)).orElse(null);
        if (item != null && item != Items.AIR) {
            VANILLA_PRICES.put(item, new long[]{min, max});
        }
    }

    private static void unobtainable(Item item) {
        if (item != null && item != Items.AIR) {
            UNOBTAINABLE_VANILLA.add(item);
        }
    }

    /**
     * 获取原版物品的固定价格范围 [min, max]。
     * 返回 null 表示该物品不是原版物品或没有预设价格。
     */
    public static long[] getVanillaPriceRange(Item item) {
        return VANILLA_PRICES.get(item);
    }

    /**
     * 判断是否为无法正常获取的原版物品（黑市专属）。
     */
    public static boolean isUnobtainable(Item item) {
        return UNOBTAINABLE_VANILLA.contains(item);
    }

    /**
     * 判断物品是否为原版物品。
     */
    public static boolean isVanilla(Item item) {
        var key = BuiltInRegistries.ITEM.getKey(item);
        return key != null && key.getNamespace().equals("minecraft");
    }

    /**
     * 判断物品是否有合成配方（可合成）。
     */
    public static boolean isCraftable(Item item) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;
        Level level = server.overworld();
        if (level == null) return false;
        RecipeManager rm = level.getRecipeManager();
        for (RecipeHolder<?> holder : rm.getAllRecipesFor(RecipeType.CRAFTING)) {
            var recipe = holder.value();
            ItemStack result = recipe.getResultItem(level.registryAccess());
            if (result.getItem() == item) return true;
        }
        return false;
    }

    /**
     * 递归分析物品的合成配方，计算基础原材料总估值。
     * 返回最低估值（CC），-1 表示无法分析。
     */
    public static long analyzeItemCost(Item item, Set<Item> visited, int maxDepth) {
        if (visited == null) visited = new HashSet<>();
        if (maxDepth <= 0) return -1;
        if (!visited.add(item)) return 0; // 循环检测，忽略

        // 原版物品：使用预设价格
        long[] vanillaRange = getVanillaPriceRange(item);
        if (vanillaRange != null) return vanillaRange[0]; // 返回最低价

        // 检查是否有合成配方
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return -1;
        Level level = server.overworld();
        if (level == null) return -1;
        RecipeManager rm = level.getRecipeManager();

        long minCost = Long.MAX_VALUE;
        boolean foundRecipe = false;

        for (RecipeHolder<?> holder : rm.getAllRecipesFor(RecipeType.CRAFTING)) {
            var recipe = holder.value();
            ItemStack result = recipe.getResultItem(level.registryAccess());
            if (result.getItem() != item) continue;

            foundRecipe = true;
            long recipeCost = 0;
            List<Ingredient> ingredients;

            if (recipe instanceof ShapedRecipe shaped) {
                ingredients = shaped.getIngredients();
            } else if (recipe instanceof ShapelessRecipe shapeless) {
                ingredients = shapeless.getIngredients();
            } else {
                continue;
            }

            for (Ingredient ing : ingredients) {
                if (ing.isEmpty()) continue;
                ItemStack[] matches = ing.getItems();
                if (matches.length == 0) continue;

                long cheapestIngredient = Long.MAX_VALUE;
                for (ItemStack match : matches) {
                    if (match.getItem() == item) continue; // 自引用忽略
                    // 该成分可能是原版物品
                    long[] subRange = getVanillaPriceRange(match.getItem());
                    if (subRange != null) {
                        cheapestIngredient = Math.min(cheapestIngredient, subRange[0]);
                    } else {
                        long subCost = analyzeItemCost(match.getItem(), new HashSet<>(visited), maxDepth - 1);
                        if (subCost > 0) cheapestIngredient = Math.min(cheapestIngredient, subCost);
                    }
                }
                if (cheapestIngredient == Long.MAX_VALUE) cheapestIngredient = 1; // 兜底
                recipeCost += cheapestIngredient;
            }

            if (recipeCost > 0) {
                minCost = Math.min(minCost, recipeCost);
            }
        }

        visited.remove(item);
        return foundRecipe ? (minCost == Long.MAX_VALUE ? 1 : minCost) : -1;
    }

    /**
     * 为模组物品生成合理的价格范围。
     * @param baseCost 基础成本（最低估值）
     * @return [minPrice, maxPrice]
     */
    public static long[] generateModItemPriceRange(long baseCost) {
        if (baseCost <= 0) return new long[]{10, 50};
        // 最低价 = 成本，最高价 = 成本的 1.5~3 倍（取决于成本高低）
        double multiplier = baseCost < 100 ? 3.0 : (baseCost < 1000 ? 2.0 : 1.5);
        long maxPrice = (long)(baseCost * multiplier);
        return new long[]{baseCost, Math.max(baseCost + 1, maxPrice)};
    }
}
