package com.unsa.gs.globalstore.core.stock;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.unsa.gs.globalstore.GlobalStore;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = GlobalStore.MODID)
public class CreditScore {
    private static final Map<UUID, Integer> SCORES = new HashMap<>();

    public static int getScore(Player player) {
        return SCORES.getOrDefault(player.getUUID(), 100);
    }

    public static void setScore(Player player, int score) {
        SCORES.put(player.getUUID(), Math.min(100, Math.max(0, score)));
    }

    public static void addScore(Player player, int delta) {
        setScore(player, getScore(player) + delta);
    }

    public static boolean canTrade(Player player) {
        return getScore(player) >= 30;
    }

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("creditrecover").then(
                Commands.argument("points", IntegerArgumentType.integer(1, 100))
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        int points = IntegerArgumentType.getInteger(ctx, "points");
                        long cost = points * 1000L; // 按10点1万CC计算，1点1000CC
                        var account = player.getData(com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
                        if (account.getBalance() >= cost) {
                            account.subtractBalance(cost);
                            addScore(player, points);
                            ctx.getSource().sendSuccess(() -> Component.literal("恢复信用分 " + points + " 点，花费 " + cost + " CC"), false);
                        } else {
                            ctx.getSource().sendFailure(Component.literal("余额不足，需要 " + cost + " CC"));
                        }
                        return 1;
                    })
            )
        );
    }
}
