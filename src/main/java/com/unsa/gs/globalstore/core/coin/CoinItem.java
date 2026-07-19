package com.unsa.gs.globalstore.core.coin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.network.chat.Component;

public class CoinItem extends Item {
    private final long coinValue;

    public CoinItem(Rarity rarity, long value) {
        super(new Item.Properties().rarity(rarity).stacksTo(64));
        this.coinValue = value;
    }

    public long getCoinValue() {
        return coinValue;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack));
    }
}
