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
        // 只在服务端处理
        if (event.getSide().isClient()) return;
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (!player.isCrouching()) return;
        if (!(stack.getItem() instanceof CoinItem coin)) return;

        long value = coin.getCoinValue() * stack.getCount();
        PlayerAccount account = player.getData(com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        account.addBalance(value);
        stack.setCount(0);  // 服务端直接清空，自动同步
        event.setCanceled(true);
    }
}
