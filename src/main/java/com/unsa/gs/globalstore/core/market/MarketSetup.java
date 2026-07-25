package com.unsa.gs.globalstore.core.market;

import com.unsa.gs.globalstore.GlobalStore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid = GlobalStore.MODID)
public class MarketSetup {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        GlobalMarket.initDefaultItems();
    }
}
