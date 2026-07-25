package com.unsa.gs.globalstore.core.bank;

import com.unsa.gs.globalstore.capability.PlayerAccount;
import net.minecraft.world.entity.player.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankManager {
    // 利息档次：存款金额（CC） vs 日利率
    public static final long[] DEPOSIT_TIERS = {100, 500, 1000, 5000, 10000, 50000, 100000, 500000, 1000000, 10000000};
    public static final double[] INTEREST_RATES = {0.001, 0.002, 0.005, 0.01, 0.02, 0.03, 0.05, 0.07, 0.1, 0.15}; // 日利率

    // 玩家存款记录
    private static final Map<UUID, BankAccount> ACCOUNTS = new HashMap<>();

    public static double getInterestRate(long deposit) {
        for (int i = DEPOSIT_TIERS.length - 1; i >= 0; i--) {
            if (deposit >= DEPOSIT_TIERS[i]) {
                return INTEREST_RATES[i];
            }
        }
        return 0.0;
    }

    public static void deposit(Player player, long amount) {
        PlayerAccount account = player.getData(com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        if (account.subtractBalance(amount)) {
            BankAccount bank = ACCOUNTS.computeIfAbsent(player.getUUID(), uuid -> new BankAccount());
            bank.deposit += amount;
        }
    }

    public static void withdraw(Player player, long amount) {
        BankAccount bank = ACCOUNTS.get(player.getUUID());
        if (bank != null && bank.deposit >= amount) {
            PlayerAccount account = player.getData(com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
            account.addBalance(amount);
            bank.deposit -= amount;
        }
    }

    public static long getDeposit(UUID uuid) {
        BankAccount bank = ACCOUNTS.get(uuid);
        return bank != null ? bank.deposit : 0;
    }

    public static void applyDailyInterest() {
        for (BankAccount bank : ACCOUNTS.values()) {
            if (bank.deposit > 0) {
                double rate = getInterestRate(bank.deposit);
                long interest = (long)(bank.deposit * rate);
                bank.deposit += interest;
            }
        }
    }

    static class BankAccount {
        long deposit = 0;
    }
}
