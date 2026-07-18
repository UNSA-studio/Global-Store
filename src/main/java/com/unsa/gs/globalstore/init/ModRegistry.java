package com.unsa.gs.globalstore.init;

import com.unsa.gs.globalstore.GlobalStore;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, GlobalStore.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, GlobalStore.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GlobalStore.MODID);

    public static final Supplier<Block> MARKET_BLOCK = BLOCKS.register("market_block",
            () -> new Block(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.WOOD)));
    public static final Supplier<Item> MARKET_BLOCK_ITEM = ITEMS.register("market_block",
            () -> new BlockItem(MARKET_BLOCK.get(), new Item.Properties()));

    public static final Supplier<CreativeModeTab> GLOBAL_STORE_TAB = TABS.register("global_store_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.globalstore"))
                    .icon(() -> MARKET_BLOCK_ITEM.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(MARKET_BLOCK_ITEM.get());
                    })
                    .build());
}
