package com.unsa.gs.globalstore.core.stock;

import net.minecraft.world.entity.player.Player;
import java.util.*;

public class PlayerCompany {
    public static final Map<UUID, String> PLAYER_COMPANIES = new HashMap<>(); // 玩家拥有的公司名
    public static final Set<UUID> CERTIFIED = new HashSet<>(); // 已获得合格证明

    public static boolean isCertified(Player player) {
        return CERTIFIED.contains(player.getUUID());
    }

    public static void grantCertification(Player player) {
        CERTIFIED.add(player.getUUID());
    }

    public static String getCompanyName(Player player) {
        return PLAYER_COMPANIES.get(player.getUUID());
    }

    public static boolean createCompany(Player player, String name, long initialPrice) {
        if (!isCertified(player)) return false;
        if (StockMarket.COMPANIES.containsKey(name)) return false; // 已存在
        StockMarket.COMPANIES.put(name, new StockCompany(name, false, initialPrice, 10000));
        PLAYER_COMPANIES.put(player.getUUID(), name);
        return true;
    }
}
