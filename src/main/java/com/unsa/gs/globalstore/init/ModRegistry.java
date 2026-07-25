package com.unsa.gs.globalstore.init;

import com.unsa.gs.globalstore.GlobalStore;
import com.unsa.gs.globalstore.core.coin.CoinRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, GlobalStore.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GlobalStore.MODID);

    public static final Supplier<CreativeModeTab> GLOBAL_STORE_TAB = TABS.register("global_store_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.globalstore"))
                    .icon(() -> new ItemStack(CoinRegistry.COMMON_CURRENCY.get()))
                    .displayItems((params, output) -> {
                        // 添加所有硬币
                        output.accept(CoinRegistry.COMMON_CURRENCY.get());
                        output.accept(CoinRegistry.TITANIUM_COIN.get());
                        output.accept(CoinRegistry.PRESSING_COIN.get());
                        output.accept(CoinRegistry.DARK_MATTER_COIN.get());
                        output.accept(CoinRegistry.COMPRESSED_DARK_MATTER_COIN.get());
                    })
                    .build());
}
