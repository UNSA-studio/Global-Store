package com.unsa.gs.globalstore.core.bank;

import com.mojang.brigadier.CommandDispatcher;
import com.unsa.gs.globalstore.GlobalStore;
import com.unsa.gs.globalstore.capability.PlayerAccount;
import com.unsa.gs.globalstore.core.stock.CreditScore;
import com.unsa.gs.globalstore.core.stock.PlayerCompany;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = GlobalStore.MODID)
public class CertifyCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("certify").executes(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                if (CreditScore.getScore(player) < 90) {
                    ctx.getSource().sendFailure(Component.literal("信用分不足90，无法申请"));
                    return 0;
                }
                PlayerAccount account = player.getData(com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
                if (account.getBalance() < 10000) {
                    ctx.getSource().sendFailure(Component.literal("需要10000 CC"));
                    return 0;
                }
                account.subtractBalance(10000);
                PlayerCompany.grantCertification(player);
                ctx.getSource().sendSuccess(() -> Component.literal("已获得合格证明！现在可以创建公司了"), false);
                return 1;
            })
        );
    }
}
