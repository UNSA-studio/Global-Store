package com.unsa.gs.globalstore.core.market;

import com.unsa.gs.globalstore.GlobalStore;
import com.unsa.gs.globalstore.core.bank.BankManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = GlobalStore.MODID)
public class DailyTasks {
    private static int tickCounter = 0;
    private static final int TICKS_PER_DAY = 24000;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter >= TICKS_PER_DAY) {
            tickCounter = 0;
            GlobalMarket.restockIfNeeded();
            BlackMarket.refreshDailyOffers();
            BankManager.applyDailyInterest();
        }
    }
}
