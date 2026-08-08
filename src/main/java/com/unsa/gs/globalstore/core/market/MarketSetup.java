package com.unsa.gs.globalstore.core.market;

import com.unsa.gs.globalstore.GlobalStore;
import com.unsa.gs.globalstore.core.stock.StockMarket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid = GlobalStore.MODID)
public class MarketSetup {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        // 全物品动态初始化
        GlobalMarket.initAllItems();
        // 黑市刷新
        BlackMarket.refreshDailyOffers();
        // 股票初始化
        StockMarket.init();
    }
}
