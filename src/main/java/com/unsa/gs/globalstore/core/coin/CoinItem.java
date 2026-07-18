package com.unsa.gs.globalstore.core.coin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.network.chat.Component;

public class CoinItem extends Item {
    private final int coinValue; // 价值以 CC 为单位

    public CoinItem(Rarity rarity, int value) {
        super(new Item.Properties().rarity(rarity).stacksTo(64));
        this.coinValue = value;
    }

    public int getCoinValue() {
        return coinValue;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack));
    }
}
