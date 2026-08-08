package com.unsa.gs.globalstore.core.market;

import com.unsa.gs.globalstore.GlobalStore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.world.item.Item;
import java.util.*;

@EventBusSubscriber(modid = GlobalStore.MODID)
public class AITrader {
    private static int tickCounter = 0;
    private static final int INTERVAL = 12000; // 半游戏日
    private static final int AI_TRADE_AMOUNT = 8; // AI 小量交易，不引起价格波动

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter >= INTERVAL) {
            tickCounter = 0;
            executeTrades();
        }
    }

    private static void executeTrades() {
        Random rand = new Random();
        List<MarketItemData> items = new ArrayList<>(GlobalMarket.MARKET_ITEMS.values());
        if (items.isEmpty()) return;
        int count = 1 + rand.nextInt(3);
        for (int i = 0; i < count; i++) {
            MarketItemData data = items.get(rand.nextInt(items.size()));
            Item item = data.getItem();
            int amount = 1 + rand.nextInt(AI_TRADE_AMOUNT);
            if (rand.nextBoolean()) {
                if (data.getStock() >= amount) {
                    GlobalMarket.buyItem(item, amount);
                }
            } else {
                GlobalMarket.sellItem(item, amount);
            }
        }
    }
}
