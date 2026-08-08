package com.unsa.gs.globalstore.core.coin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class CoinItem extends Item {
    private final long coinValue;

    public CoinItem(Rarity rarity, long value) {
        super(new Item.Properties().rarity(rarity).stacksTo(64));
        this.coinValue = value;
    }

    public long getCoinValue() { return coinValue; }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("面值: " + String.format("%,d", coinValue) + " CC")
            .withStyle(net.minecraft.ChatFormatting.GOLD));
        if (stack.getCount() > 1)
            tooltip.add(Component.literal("堆叠总价值: " + String.format("%,d", coinValue * stack.getCount()) + " CC")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
