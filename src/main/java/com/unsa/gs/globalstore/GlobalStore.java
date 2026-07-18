package com.unsa.gs.globalstore;

import com.mojang.logging.LogUtils;
import com.unsa.gs.globalstore.init.ModRegistry;
import com.unsa.gs.globalstore.core.coin.CoinRegistry;
import com.unsa.gs.globalstore.capability.AccountCapability;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(GlobalStore.MODID)
public class GlobalStore {
    public static final String MODID = "globalstore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GlobalStore(IEventBus modEventBus) {
        ModRegistry.BLOCKS.register(modEventBus);
        ModRegistry.ITEMS.register(modEventBus);
        ModRegistry.TABS.register(modEventBus);
        CoinRegistry.COINS.register(modEventBus);
        AccountCapability.ATTACHMENTS.register(modEventBus);
    }
}
