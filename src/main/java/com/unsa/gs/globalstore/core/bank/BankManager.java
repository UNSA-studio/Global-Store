package com.unsa.gs.globalstore.core.bank;

import com.unsa.gs.globalstore.capability.PlayerAccount;
import com.unsa.gs.globalstore.core.coin.CoinRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class BankManager {
    public static final long[] DEPOSIT_TIERS = {100, 500, 1000, 5000, 10000, 50000, 100000, 500000, 1000000, 10000000};
    public static final double[] INTEREST_RATES = {0.001, 0.002, 0.005, 0.01, 0.02, 0.03, 0.05, 0.07, 0.1, 0.15};
    private static final Map<UUID, BankAccount> ACCOUNTS = new HashMap<>();

    // 硬币面值从大到小排列（用于铸币）
    private static final Item[] COIN_DENOMINATIONS;
    private static final long[] COIN_VALUES;

    static {
        COIN_DENOMINATIONS = new Item[] {
            CoinRegistry.COMPRESSED_DARK_MATTER_COIN.get(),
            CoinRegistry.DARK_MATTER_COIN.get(),
            CoinRegistry.PRESSING_COIN.get(),
            CoinRegistry.TITANIUM_COIN.get(),
            CoinRegistry.COMMON_CURRENCY.get()
        };
        COIN_VALUES = new long[] { 100_000_000_000L, 1_000_000_000L, 100_000L, 100L, 1L };
    }

    public static double getInterestRate(long deposit) {
        for (int i = DEPOSIT_TIERS.length - 1; i >= 0; i--)
            if (deposit >= DEPOSIT_TIERS[i]) return INTEREST_RATES[i];
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

    /**
     * 铸币：将账户余额兑换为硬币物品（大面额优先）。
     * @return 铸币结果消息
     */
    public static String mintCoins(ServerPlayer player, long amount) {
        PlayerAccount account = player.getData(com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        if (amount <= 0 || account.getBalance() < amount) return "余额不足";

        long remaining = amount;
        account.subtractBalance(amount);
        int totalCoins = 0;

        for (int i = 0; i < COIN_VALUES.length && remaining > 0; i++) {
            long value = COIN_VALUES[i];
            int count = (int)(remaining / value);
            if (count > 0) {
                ItemStack stack = new ItemStack(COIN_DENOMINATIONS[i], count);
                if (!player.addItem(stack)) player.drop(stack, false);
                remaining -= count * value;
                totalCoins += count;
            }
        }

        if (remaining > 0) {
            account.addBalance(remaining); // 退回零头
        }

        return "铸币 " + String.format("%,d", amount) + " CC → " + totalCoins + " 枚硬币";
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

    static class BankAccount { long deposit = 0; }
}
