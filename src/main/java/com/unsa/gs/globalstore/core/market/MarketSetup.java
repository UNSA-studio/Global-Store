package com.unsa.gs.globalstore.core.market;

import com.unsa.gs.globalstore.GlobalStore;
import com.unsa.gs.globalstore.config.ModConfigs;
import com.unsa.gs.globalstore.core.stock.StockMarket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.world.item.Items;

@EventBusSubscriber(modid = GlobalStore.MODID)
public class MarketSetup {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        GlobalMarket.initDefaultItems();
        // 应用配置覆盖价格
        for (var entry : GlobalMarket.MARKET_ITEMS.entrySet()) {
            String key = entry.getValue().getItem().getDescription().getString().toLowerCase().replace(" ", "_");
            if (ModConfigs.minPrices.containsKey(key)) {
                long min = ModConfigs.minPrices.get(key).get();
                long max = ModConfigs.maxPrices.get(key).get();
                // 由于 MarketItemData 构造后不能改 min/max，我们简单地在 updatePrices 里做限制，但这里可以不覆盖
            }
        }
        BlackMarket.refreshDailyOffers();
        StockMarket.init();
    }
}
