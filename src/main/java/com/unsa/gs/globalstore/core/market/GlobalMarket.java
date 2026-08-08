package com.unsa.gs.globalstore.core.market;

import com.unsa.gs.globalstore.GlobalStore;
import com.unsa.gs.globalstore.capability.PlayerAccount;
import com.unsa.gs.globalstore.core.market.recipe.RecipeAnalyzer;
import com.unsa.gs.globalstore.core.stock.CreditScore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.*;

public class GlobalMarket {
    public static final Map<Item, MarketItemData> MARKET_ITEMS = new LinkedHashMap<>();

    public static void initAllItems() {
        MARKET_ITEMS.clear();
        Set<Item> craftableItems = new HashSet<>();
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            Level level = server.overworld();
            if (level != null) {
                for (var holder : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
                    ItemStack result = holder.value().getResultItem(level.registryAccess());
                    if (result.getItem() != Items.AIR) craftableItems.add(result.getItem());
                }
            }
        }
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            if (RecipeAnalyzer.isUnobtainable(item)) continue;
            if (isCoinItem(item)) continue;
            long[] vanillaRange = RecipeAnalyzer.getVanillaPriceRange(item);
            if (vanillaRange != null) {
                addItem(new ItemStack(item), vanillaRange[0], vanillaRange[1],
                    calcInitialStock(item, vanillaRange[0]), false);
                continue;
            }
            if (RecipeAnalyzer.isVanilla(item)) {
                if (craftableItems.contains(item))
                    addItem(new ItemStack(item), 5, 20, 32, false);
                continue;
            }
            if (craftableItems.contains(item)) {
                long baseCost = RecipeAnalyzer.analyzeItemCost(item, null, 6);
                long[] range = baseCost > 0 ? RecipeAnalyzer.generateModItemPriceRange(baseCost)
                    : new long[]{10, 50};
                addItem(new ItemStack(item), range[0], range[1],
                    calcInitialStock(item, range[0]), true);
            }
        }
    }

    @Deprecated
    public static void initDefaultItems() {
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

    private static void addItem(ItemStack stack, long min, long max, int stock, boolean isModded) {
        if (MARKET_ITEMS.containsKey(stack.getItem())) return;
        MarketItemData data = new MarketItemData(stack, min, max, isModded);
        data.setStock(stock);
        MARKET_ITEMS.put(stack.getItem(), data);
    }

    private static int calcInitialStock(Item item, long minPrice) {
        if (minPrice >= 500) return 8;
        if (minPrice >= 100) return 16;
        if (minPrice >= 10) return 64;
        return 128;
    }

    private static boolean isCoinItem(Item item) {
        var key = BuiltInRegistries.ITEM.getKey(item);
        return key != null && key.getNamespace().equals("globalstore") &&
            (key.getPath().contains("coin") || key.getPath().contains("currency"));
    }

    public static MarketItemData getData(Item item) { return MARKET_ITEMS.get(item); }

    // ==================== 完整交易逻辑 ====================

    /**
     * 玩家从市场买入物品。
     * @return null=成功, 否则返回错误消息
     */
    public static String playerBuy(ServerPlayer player, Item item, int amount) {
        if (!CreditScore.canTrade(player)) return "信用分不足30，禁止交易";

        MarketItemData data = getData(item);
        if (data == null) return "该物品不在市场中";
        if (data.getStock() < amount) return "市场库存不足";

        PlayerAccount account = player.getData(
            com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        long totalCost = data.getCurrentSellPrice() * amount;
        if (!account.subtractBalance(totalCost)) return "余额不足，需要 " + totalCost + " CC";

        // 反操纵检查
        long fine = TradeMonitor.recordTrade(player, item, amount, true);
        if (fine > 0) return "交易被监控拦截: 罚款 " + fine + " CC";

        data.setStock(data.getStock() - amount);
        data.recordTrade(true, amount);

        ItemStack stack = new ItemStack(item, amount);
        if (!player.addItem(stack)) player.drop(stack, false);

        GlobalStore.LOGGER.info("{} bought {}x {} for {} CC", player.getName().getString(),
            amount, item.getDescription().getString(), totalCost);
        return null; // success
    }

    /**
     * 玩家向市场卖出物品。
     */
    public static String playerSell(ServerPlayer player, Item item, int amount) {
        if (!CreditScore.canTrade(player)) return "信用分不足30，禁止交易";

        MarketItemData data = getData(item);
        if (data == null) return "该物品不在市场中";

        // 从玩家背包移除物品
        int removed = removeItemsFromPlayer(player, item, amount);
        if (removed <= 0) return "背包中没有足够的 " + item.getDescription().getString();

        // 反操纵检查
        long fine = TradeMonitor.recordTrade(player, item, removed, false);
        if (fine > 0) {
            // 退还被移除的物品
            player.addItem(new ItemStack(item, removed));
            return "交易被监控拦截: 罚款 " + fine + " CC";
        }

        PlayerAccount account = player.getData(
            com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        long totalRevenue = data.getCurrentBuyPrice() * removed;
        account.addBalance(totalRevenue);

        data.setStock(data.getStock() + removed);
        data.recordTrade(false, removed);

        GlobalStore.LOGGER.info("{} sold {}x {} for {} CC", player.getName().getString(),
            removed, item.getDescription().getString(), totalRevenue);
        return null;
    }

    /**
     * 回收物品（市场价的60%）。
     */
    public static String playerRecycle(ServerPlayer player, Item item, int amount) {
        MarketItemData data = getData(item);
        if (data == null) return "该物品不可回收";

        int removed = removeItemsFromPlayer(player, item, amount);
        if (removed <= 0) return "背包中没有足够的 " + item.getDescription().getString();

        long price = (long)(data.getCurrentBuyPrice() * 0.6);
        long total = price * removed;
        PlayerAccount account = player.getData(
            com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        account.addBalance(total);

        data.setStock(data.getStock() + removed);
        return null; // 回收成功，但不影响价格
    }

    private static int removeItemsFromPlayer(ServerPlayer player, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                int take = Math.min(stack.getCount(), remaining);
                stack.shrink(take);
                remaining -= take;
            }
        }
        return amount - remaining;
    }

    /** @deprecated 使用 playerBuy/playerSell */
    @Deprecated
    public static boolean buyItem(Item item, int amount) {
        MarketItemData data = getData(item);
        if (data == null || data.getStock() < amount) return false;
        data.setStock(data.getStock() - amount);
        data.recordTrade(true, amount);
        return true;
    }

    @Deprecated
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
