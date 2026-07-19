package com.unsa.gs.globalstore.core.coin;

import com.unsa.gs.globalstore.GlobalStore;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

import java.util.function.Supplier;

public class CoinRegistry {
    public static final DeferredRegister<Item> COINS = DeferredRegister.create(Registries.ITEM, GlobalStore.MODID);

    public static final Supplier<Item> COMMON_CURRENCY = COINS.register("common_currency",
            () -> new CoinItem(Rarity.COMMON, 1L));
    public static final Supplier<Item> TITANIUM_COIN = COINS.register("titanium_coin",
            () -> new CoinItem(Rarity.UNCOMMON, 100L));
    public static final Supplier<Item> PRESSING_COIN = COINS.register("pressing_coin",
            () -> new CoinItem(Rarity.RARE, 100_000L));
    public static final Supplier<Item> DARK_MATTER_COIN = COINS.register("dark_matter_coin",
            () -> new CoinItem(Rarity.EPIC, 1_000_000_000L));
    public static final Supplier<Item> COMPRESSED_DARK_MATTER_COIN = COINS.register("compressed_dark_matter_coin",
            () -> new CoinItem(Rarity.EPIC, 100_000_000_000L));
}
