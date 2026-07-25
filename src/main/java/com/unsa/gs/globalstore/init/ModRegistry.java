package com.unsa.gs.globalstore.init;

import com.unsa.gs.globalstore.GlobalStore;
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

    // 创造模式标签页，暂时放一个通用物品占位（可换为货币物品）
    public static final Supplier<CreativeModeTab> GLOBAL_STORE_TAB = TABS.register("global_store_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.globalstore"))
                    .icon(() -> new ItemStack(net.minecraft.world.item.Items.EMERALD)) // 临时用绿宝石
                    .displayItems((params, output) -> {
                        // 以后添加货币等物品
                    })
                    .build());
}
