package com.unsa.gs.globalstore.client;

import com.unsa.gs.globalstore.GlobalStore;
import com.unsa.gs.globalstore.core.market.GlobalMarket;
import com.unsa.gs.globalstore.core.market.MarketItemData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = GlobalStore.MODID, value = Dist.CLIENT)
public class TooltipHandler {
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        MarketItemData data = GlobalMarket.getData(item);
        if (data != null) {
            event.getToolTip().add(Component.literal("买入价: " + data.getCurrentSellPrice() + " CC"));
            event.getToolTip().add(Component.literal("卖出价: " + data.getCurrentBuyPrice() + " CC"));
        }
    }
}
