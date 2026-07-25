package com.unsa.gs.globalstore.core.coin;

import com.unsa.gs.globalstore.GlobalStore;
import com.unsa.gs.globalstore.capability.PlayerAccount;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = GlobalStore.MODID)
public class CoinInteractionHandler {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        // 检查是否潜行（蹲下）
        if (!player.isCrouching()) return;
        // 检查是否是我们的硬币
        if (!(stack.getItem() instanceof CoinItem coin)) return;

        // 计算硬币总价值
        long value = coin.getCoinValue() * stack.getCount();
        PlayerAccount account = player.getData(com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        account.addBalance(value);

        // 消耗手中硬币
        stack.setCount(0);

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
