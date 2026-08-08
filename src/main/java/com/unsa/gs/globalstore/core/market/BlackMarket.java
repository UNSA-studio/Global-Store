package com.unsa.gs.globalstore.core.market;

import com.unsa.gs.globalstore.core.market.recipe.RecipeAnalyzer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

/**
 * 黑市 — 专门售卖无法通过正常途径获取的物品。
 * 价格完全随机，无库存限制，每个玩家限量购买 10 个。
 * 每天发放 5-10 个市场高价值物品，价格低于市场 60%，先到先得。
 */
public class BlackMarket {
    // 当天黑市商品列表
    public static final List<BlackMarketOffer> DAILY_OFFERS = new ArrayList<>();

    // 每个玩家对每个物品的已购买数量限制
    public static final int PLAYER_BUY_LIMIT = 10;

    // 玩家已购买记录：UUID -> 物品 -> 已购数量
    private static final Map<UUID, Map<Item, Integer>> PLAYER_PURCHASES = new HashMap<>();

    // 每日特价商品（市场中高价值物品，限量，先到先得）
    public static final List<DailySpecialOffer> DAILY_SPECIALS = new ArrayList<>();

    /**
     * 黑市物品池 — 不可正常获取 + 部分稀有掉落/探索品。
     */
    private static final List<ItemStack> BLACK_MARKET_POOL = new ArrayList<>();

    static {
        // 原版不可获取物品
        addPoolItem(Items.BEDROCK, 1);
        addPoolItem(Items.BARRIER, 1);
        addPoolItem(Items.DRAGON_EGG, 1);
        addPoolItem(Items.END_PORTAL_FRAME, 1);
        addPoolItem(Items.SPAWNER, 1);
        addPoolItem(Items.BUDDING_AMETHYST, 1);
        addPoolItem(Items.REINFORCED_DEEPSLATE, 1);
        addPoolItem(Items.ECHO_SHARD, 1);

        // 稀有/特殊获取品
        addPoolItem(Items.ELYTRA, 1);
        addPoolItem(Items.ENCHANTED_GOLDEN_APPLE, 1);
        addPoolItem(Items.NETHER_STAR, 1);
        addPoolItem(Items.TOTEM_OF_UNDYING, 1);
        addPoolItem(Items.HEART_OF_THE_SEA, 1);
        addPoolItem(Items.TRIDENT, 1);
        addPoolItem(Items.DISC_FRAGMENT_5, 1);
    }

    private static void addPoolItem(Item item, int count) {
        BLACK_MARKET_POOL.add(new ItemStack(item, count));
    }

    /**
     * 刷新每日商品：随机选 4-8 个物品，价格完全随机。
     * 同时生成 5-10 个市场高价值特价商品。
     */
    public static void refreshDailyOffers() {
        DAILY_OFFERS.clear();
        DAILY_SPECIALS.clear();
        PLAYER_PURCHASES.clear();
        Random rand = new Random();

        // 动态检查模组中不可获取的物品，加入黑市池
        // 复用GlobalMarket已缓存的可合成物品集合来加速
        Set<Item> marketItemSet = new HashSet<>(GlobalMarket.MARKET_ITEMS.keySet());
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            if (marketItemSet.contains(item)) continue;
            var key = BuiltInRegistries.ITEM.getKey(item);
            if (key != null && key.getNamespace().equals("globalstore")) continue;
            // 无法合成的模组物品加入黑市
            if (!RecipeAnalyzer.isVanilla(item) && !marketItemSet.contains(item) &&
                !RecipeAnalyzer.isCraftable(item)) {
                boolean alreadyInPool = BLACK_MARKET_POOL.stream()
                    .anyMatch(s -> s.getItem() == item);
                if (!alreadyInPool) {
                    BLACK_MARKET_POOL.add(new ItemStack(item, 1));
                }
            }
        }

        // 随机选择 4-8 个物品作为今日黑市商品
        List<ItemStack> shuffled = new ArrayList<>(BLACK_MARKET_POOL);
        Collections.shuffle(shuffled, rand);
        int count = 4 + rand.nextInt(5); // 4-8
        for (int i = 0; i < Math.min(count, shuffled.size()); i++) {
            ItemStack stack = shuffled.get(i).copy();
            // 价格完全随机：范围极大（100-10000000 CC）
            long price = 100 + (long)(rand.nextDouble() * 10_000_000);
            // 大多数物品极其贵
            if (rand.nextInt(100) < 70) {
                price = 10_000 + (long)(rand.nextDouble() * 5_000_000);
            }
            DAILY_OFFERS.add(new BlackMarketOffer(stack.copy(), price));
        }

        // 生成 5-10 个市场高价值特价商品
        List<MarketItemData> marketItems = new ArrayList<>(GlobalMarket.MARKET_ITEMS.values());
        if (marketItems.isEmpty()) return;
        // 按价格排序，取高价值物品（前 30%）
        marketItems.sort((a, b) -> Long.compare(b.getCurrentSellPrice(), a.getCurrentSellPrice()));
        int highValueCount = Math.max(5, marketItems.size() / 3);
        List<MarketItemData> highValueItems = marketItems.subList(0, Math.min(highValueCount, marketItems.size()));
        Collections.shuffle(highValueItems, rand);

        int specialCount = 5 + rand.nextInt(6); // 5-10
        for (int i = 0; i < Math.min(specialCount, highValueItems.size()); i++) {
            MarketItemData data = highValueItems.get(i);
            long marketPrice = data.getCurrentSellPrice();
            long specialPrice = (long)(marketPrice * 0.4); // 低于市场 60%
            int stock = 1 + rand.nextInt(3); // 每人限购1-3个，总量有限
            DAILY_SPECIALS.add(new DailySpecialOffer(
                new ItemStack(data.getItem()), specialPrice, stock, 1 + rand.nextInt(3)));
        }
    }

    /**
     * 玩家从黑市购买物品。
     * @return 成功返回 true
     */
    public static boolean playerBuy(Player player, Item item, long price) {
        // 检查是否在黑市商品中
        BlackMarketOffer offer = findOffer(item);
        if (offer == null) return false;

        // 检查限量
        int alreadyBought = getPlayerPurchaseCount(player, item);
        if (alreadyBought >= PLAYER_BUY_LIMIT) return false;

        // 扣除余额
        var account = player.getData(
            com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        if (!account.subtractBalance(price)) return false;

        // 记录购买
        PLAYER_PURCHASES.computeIfAbsent(player.getUUID(), k -> new HashMap<>())
            .merge(item, 1, Integer::sum);

        // 给玩家物品
        ItemStack stack = offer.item.copy();
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }

        // 黑市价格不受影响（不动）
        return true;
    }

    /**
     * 玩家抢购每日特价商品。
     */
    public static boolean playerBuySpecial(Player player, int specialIndex) {
        if (specialIndex < 0 || specialIndex >= DAILY_SPECIALS.size()) return false;
        DailySpecialOffer special = DAILY_SPECIALS.get(specialIndex);
        if (special.remainingStock <= 0) return false;

        // 玩家限量
        int alreadyBought = getPlayerPurchaseCount(player, special.item.getItem());
        if (alreadyBought >= special.perPlayerLimit) return false;

        var account = player.getData(
            com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        if (!account.subtractBalance(special.price)) return false;

        special.remainingStock--;
        PLAYER_PURCHASES.computeIfAbsent(player.getUUID(), k -> new HashMap<>())
            .merge(special.item.getItem(), 1, Integer::sum);

        ItemStack stack = special.item.copy();
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
        return true;
    }

    private static BlackMarketOffer findOffer(Item item) {
        for (BlackMarketOffer offer : DAILY_OFFERS) {
            if (offer.item.getItem() == item) return offer;
        }
        return null;
    }

    private static int getPlayerPurchaseCount(Player player, Item item) {
        Map<Item, Integer> map = PLAYER_PURCHASES.get(player.getUUID());
        if (map == null) return 0;
        return map.getOrDefault(item, 0);
    }

    // --- 数据结构 ---

    public static class BlackMarketOffer {
        public ItemStack item;      // 注意：数量代表每次购买得到的数量
        public long priceInCC;

        public BlackMarketOffer(ItemStack item, long priceInCC) {
            this.item = item;
            this.priceInCC = priceInCC;
        }
    }

    public static class DailySpecialOffer {
        public ItemStack item;
        public long price;
        public int remainingStock;
        public int perPlayerLimit;

        public DailySpecialOffer(ItemStack item, long price, int stock, int perPlayerLimit) {
            this.item = item;
            this.price = price;
            this.remainingStock = stock;
            this.perPlayerLimit = perPlayerLimit;
        }
    }
}
