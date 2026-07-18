package com.unsa.gs.globalstore.core.coin;

import com.unsa.gs.globalstore.GlobalStore;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CoinRegistry {
    public static final DeferredRegister<Item> COINS = DeferredRegister.create(Registries.ITEM, GlobalStore.MODID);

    public static final Supplier<Item> COMMON_CURRENCY = COINS.register("common_currency",
            () -> new CoinItem(Rarity.COMMON, 1)); // 1 CC
    public static final Supplier<Item> TITANIUM_COIN = COINS.register("titanium_coin",
            () -> new CoinItem(Rarity.UNCOMMON, 100)); // 100 CC
    public static final Supplier<Item> PRESSING_COIN = COINS.register("pressing_coin",
            () -> new CoinItem(Rarity.RARE, 100_000)); // 100 TC = 100 * 1000? 按设定：CC:TC=100:1, TC:PC=1000:1 => 1 PC = 100,000 CC
    public static final Supplier<Item> DARK_MATTER_COIN = COINS.register("dark_matter_coin",
            () -> new CoinItem(Rarity.EPIC, 1_000_000_000)); // 1 DMC = 10000 PC = 1e9 CC (检查：100,000*10000=1e9)
    public static final Supplier<Item> COMPRESSED_DARK_MATTER_COIN = COINS.register("compressed_dark_matter_coin",
            () -> new CoinItem(Rarity.EPIC, 100_000_000_000L)); // 1 CDMC = 100000 DMC = 1e14 CC，暂用long，但ItemStack无法存大于int的值，实际实现需小心，这里先用long表示价值，存储时转换。
}
