package com.unsa.gs.globalstore.core.stock;

import com.unsa.gs.globalstore.capability.PlayerAccount;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * 玩家公司系统。合格证明操作从命令迁移到 GUI 银行标签页。
 */
public class PlayerCompany {
    public static final Map<UUID, String> PLAYER_COMPANIES = new HashMap<>();
    public static final Set<UUID> CERTIFIED = new HashSet<>();

    public static boolean isCertified(Player player) {
        return CERTIFIED.contains(player.getUUID());
    }

    public static void grantCertification(Player player) {
        CERTIFIED.add(player.getUUID());
    }

    public static String getCompanyName(Player player) {
        return PLAYER_COMPANIES.get(player.getUUID());
    }

    /**
     * 从 GUI 调用：申请合格证明。
     * 条件：信用分 >= 90，余额 >= 10000 CC。
     * @return 成功返回 true
     */
    public static boolean requestCertification(Player player) {
        if (CERTIFIED.contains(player.getUUID())) return false; // 已认证
        if (CreditScore.getScore(player) < 90) return false;
        PlayerAccount account = player.getData(
            com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        if (account.getBalance() < 10000) return false;
        account.subtractBalance(10000);
        grantCertification(player);
        return true;
    }

    public static boolean createCompany(Player player, String name, long initialPrice) {
        if (!isCertified(player)) return false;
        if (StockMarket.COMPANIES.containsKey(name)) return false;
        StockMarket.COMPANIES.put(name, new StockCompany(name, false, initialPrice, 10000));
        PLAYER_COMPANIES.put(player.getUUID(), name);
        return true;
    }
}
