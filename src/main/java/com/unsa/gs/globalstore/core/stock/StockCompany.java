package com.unsa.gs.globalstore.core.stock;

import com.unsa.gs.globalstore.capability.PlayerAccount;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.*;

public class StockCompany {
    public final String name;
    public final boolean isRealWorld;
    private long currentPrice;
    private long volatility;
    private long totalShares;
    private long availableShares;
    // 玩家持股: UUID -> 股数
    private final Map<UUID, Long> shareholders = new HashMap<>();

    public StockCompany(String name, boolean isRealWorld, long initialPrice, long totalShares) {
        this.name = name;
        this.isRealWorld = isRealWorld;
        this.currentPrice = initialPrice;
        this.volatility = Math.max(1, initialPrice / 10);
        this.totalShares = totalShares;
        this.availableShares = totalShares;
    }

    public long getPrice() { return currentPrice; }
    public long getAvailableShares() { return availableShares; }
    public long getTotalShares() { return totalShares; }
    public long getPlayerShares(UUID uuid) { return shareholders.getOrDefault(uuid, 0L); }

    public void adjustPrice(long delta) {
        this.currentPrice = Math.max(1, currentPrice + delta);
        this.volatility = Math.max(1, currentPrice / 10);
    }

    public boolean buyShares(ServerPlayer player, long shares) {
        if (shares <= 0 || shares > availableShares) return false;
        long cost = currentPrice * shares;
        PlayerAccount account = player.getData(
            com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        if (!account.subtractBalance(cost)) return false;
        availableShares -= shares;
        shareholders.merge(player.getUUID(), shares, Long::sum);
        // 大量买入推动股价上涨
        adjustPrice(shares / 10);
        return true;
    }

    public boolean sellShares(ServerPlayer player, long shares) {
        if (shares <= 0) return false;
        long owned = getPlayerShares(player.getUUID());
        if (shares > owned) return false;
        long revenue = currentPrice * shares;
        PlayerAccount account = player.getData(
            com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        account.addBalance(revenue);
        availableShares += shares;
        shareholders.merge(player.getUUID(), -shares, Long::sum);
        // 大量卖出压低股价
        adjustPrice(-shares / 10);
        return true;
    }

    public void tick() {
        Random rand = new Random();
        if (rand.nextInt(100) < 30) {
            long change = (long)(rand.nextGaussian() * volatility);
            adjustPrice(change);
        }
    }

    public Map<UUID, Long> getShareholders() { return Collections.unmodifiableMap(shareholders); }
}
