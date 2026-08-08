package com.unsa.gs.globalstore.core.market;

import com.unsa.gs.globalstore.GlobalStore;
import com.unsa.gs.globalstore.capability.PlayerAccount;
import com.unsa.gs.globalstore.core.stock.CreditScore;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 交易监控器 — 反恶意操纵系统。
 * 检测异常抬价/压价行为，执行罚款、扣信用分、交易限制。
 */
public class TradeMonitor {
    // 大宗交易阈值：单次交易超过此数量视为大宗
    public static final int BULK_THRESHOLD = 32;

    // 恶意操纵阈值：短时间内同一物品交易次数
    public static final int MANIPULATION_THRESHOLD = 5;

    // 基础冷却时间（毫秒）
    public static final long BASE_COOLDOWN_MS = TimeUnit.MINUTES.toMillis(5);

    // 堆叠冷却增量（毫秒）
    public static final long COOLDOWN_INCREMENT_MS = TimeUnit.MINUTES.toMillis(10);

    // 最大冷却时间（毫秒）= 48小时
    public static final long MAX_COOLDOWN_MS = TimeUnit.HOURS.toMillis(48);

    // 玩家交易记录：UUID -> 物品 -> 交易记录列表
    private static final Map<UUID, Map<Item, List<TradeRecord>>> PLAYER_TRADE_HISTORY = new HashMap<>();

    // 玩家冷却记录：UUID -> 物品 -> (冷却结束时间戳, 违规次数)
    private static final Map<UUID, Map<Item, CooldownEntry>> PLAYER_COOLDOWNS = new HashMap<>();

    /**
     * 记录一次交易并检查是否违规。
     * @return 违规时返回罚金，否则返回0。
     */
    public static long recordTrade(Player player, Item item, int amount, boolean isBuy) {
        UUID uuid = player.getUUID();
        long now = System.currentTimeMillis();

        // 检查冷却
        CooldownEntry cooldown = getCooldown(uuid, item);
        if (cooldown != null && now < cooldown.endTime) {
            // 在冷却期内交易：严重违规
            return handleViolation(player, item, 5000, 5,
                "冷却期内大额交易: " + item.getDescription().getString());
        }

        // 记录交易
        Map<Item, List<TradeRecord>> itemMap = PLAYER_TRADE_HISTORY
            .computeIfAbsent(uuid, k -> new HashMap<>());
        List<TradeRecord> records = itemMap.computeIfAbsent(item, k -> new ArrayList<>());

        // 清理过期记录（5分钟窗口）
        records.removeIf(r -> now - r.timestamp > TimeUnit.MINUTES.toMillis(5));
        records.add(new TradeRecord(amount, now));

        // 判断是否大宗交易
        if (amount >= BULK_THRESHOLD) {
            // 检查是否短时间高频率交易同一物品
            int recentTrades = 0;
            long windowStart = now - TimeUnit.MINUTES.toMillis(5);
            for (TradeRecord r : records) {
                if (r.timestamp >= windowStart) recentTrades++;
            }

            if (recentTrades >= MANIPULATION_THRESHOLD) {
                // 恶意操纵检测
                return handleViolation(player, item,
                    10000 + recentTrades * 5000L,  // 罚款递增
                    10,                             // 扣信用分
                    "疑似恶意操纵市场: " + item.getDescription().getString());
            }

            // 正常大宗交易：施加冷却
            addCooldown(player, item);
        }

        return 0; // 无违规
    }

    private static long handleViolation(Player player, Item item, long fine, int creditLoss, String reason) {
        PlayerAccount account = player.getData(
            com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        if (account == null) return 0;
        // 防止余额变负：余额不足时仅扣到0
        long actualFine = Math.min(fine, account.getBalance());
        account.subtractBalance(actualFine);
        CreditScore.addScore(player, -creditLoss);
        GlobalStore.LOGGER.warn("Trade violation: {} | Fine: {} CC | Credit: -{}",
            reason, actualFine, creditLoss);
        return actualFine;
    }

    private static CooldownEntry getCooldown(UUID uuid, Item item) {
        Map<Item, CooldownEntry> itemMap = PLAYER_COOLDOWNS.get(uuid);
        if (itemMap == null) return null;
        return itemMap.get(item);
    }

    private static void addCooldown(Player player, Item item) {
        UUID uuid = player.getUUID();
        Map<Item, CooldownEntry> itemMap = PLAYER_COOLDOWNS
            .computeIfAbsent(uuid, k -> new HashMap<>());

        CooldownEntry existing = itemMap.get(item);
        long now = System.currentTimeMillis();
        if (existing != null && now < existing.endTime) {
            // 叠加违规
            existing.violations++;
            long extraMs = COOLDOWN_INCREMENT_MS * existing.violations;
            existing.endTime = now + Math.min(BASE_COOLDOWN_MS + extraMs, MAX_COOLDOWN_MS);
        } else {
            itemMap.put(item, new CooldownEntry(now + BASE_COOLDOWN_MS, 1));
        }
    }

    /**
     * 检查玩家对某物品是否处于交易冷却。
     */
    public static boolean isOnCooldown(Player player, Item item) {
        CooldownEntry entry = getCooldown(player.getUUID(), item);
        if (entry == null) return false;
        if (System.currentTimeMillis() >= entry.endTime) {
            // 冷却已过期，清除
            Map<Item, CooldownEntry> map = PLAYER_COOLDOWNS.get(player.getUUID());
            if (map != null) map.remove(item);
            return false;
        }
        return true;
    }

    /**
     * 获取冷却剩余时间（毫秒），没有冷却返回0。
     */
    public static long getCooldownRemaining(Player player, Item item) {
        CooldownEntry entry = getCooldown(player.getUUID(), item);
        if (entry == null) return 0;
        long remaining = entry.endTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    // 数据结构

    private static class TradeRecord {
        final int amount;
        final long timestamp;

        TradeRecord(int amount, long timestamp) {
            this.amount = amount;
            this.timestamp = timestamp;
        }
    }

    private static class CooldownEntry {
        long endTime;
        int violations;

        CooldownEntry(long endTime, int violations) {
            this.endTime = endTime;
            this.violations = violations;
        }
    }

    /**
     * 清理离线玩家数据（可选，防止内存泄漏）。
     */
    public static void cleanupPlayer(UUID uuid) {
        PLAYER_TRADE_HISTORY.remove(uuid);
        PLAYER_COOLDOWNS.remove(uuid);
    }
}