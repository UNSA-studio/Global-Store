package com.unsa.gs.globalstore.core.stock;

import com.unsa.gs.globalstore.GlobalStore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.*;

@EventBusSubscriber(modid = GlobalStore.MODID)
public class StockMarket {
    public static final Map<String, StockCompany> COMPANIES = new LinkedHashMap<>();

    public static void init() {
        // 三大科技股
        COMPANIES.put("MSFT", new StockCompany("Microsoft", true, 10000, 100000));
        COMPANIES.put("AAPL", new StockCompany("Apple", true, 12000, 100000));
        COMPANIES.put("GOOGL", new StockCompany("Google", true, 15000, 100000));
        // 随机公司
        COMPANIES.put("MINEC", new StockCompany("MineCorp", false, 500, 50000));
        COMPANIES.put("BLOCK", new StockCompany("BlockTech", false, 2000, 30000));
        COMPANIES.put("ENDER", new StockCompany("Ender Innovations", false, 800, 40000));
        COMPANIES.put("REDST", new StockCompany("Redstone Dynamics", false, 3500, 20000));
    }

    private static int tickCounter = 0;
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter % 600 == 0) { // 每30秒波动一次
            for (StockCompany company : COMPANIES.values()) {
                company.tick();
            }
        }
    }
}
