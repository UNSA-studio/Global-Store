package com.unsa.gs.globalstore.gui;

import com.unsa.gs.globalstore.GlobalStore;
import com.unsa.gs.globalstore.core.market.*;
import com.unsa.gs.globalstore.core.bank.BankManager;
import com.unsa.gs.globalstore.core.lottery.LotterySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MarketScreen extends Screen {
    private static final int WIDTH = 248;
    private static final int HEIGHT = 166;
    private static final String[] TAB_NAMES = {"Market", "Black Market", "Recycle", "Bank", "Lottery"};
    private int guiLeft, guiTop;
    private int selectedTab = 0;
    private List<MarketItemData> marketItems = new ArrayList<>();

    public MarketScreen() {
        super(Component.translatable("gui.globalstore.market"));
        GlobalMarket.initDefaultItems();
        marketItems = new ArrayList<>(GlobalMarket.MARKET_ITEMS.values());
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - WIDTH) / 2;
        this.guiTop = (this.height - HEIGHT) / 2;

        for (int i = 0; i < 5; i++) {
            final int tabIndex = i;
            addRenderableWidget(Button.builder(Component.literal(TAB_NAMES[i]), btn -> {
                selectedTab = tabIndex;
                rebuildWidgets();
            }).pos(guiLeft + 8 + i * 46, guiTop + 6).size(42, 16).build());
        }

        // 示例：在抽奖选项卡添加抽奖按钮
        if (selectedTab == 4) {
            addRenderableWidget(Button.builder(Component.literal("Spin (10CC)"), btn -> {
                // 客户端抽奖演示，实际应发包给服务器
                long result = LotterySystem.roll(10);
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Lottery: " + result + "CC"));
            }).pos(guiLeft + 80, guiTop + 100).size(80, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(guiLeft, guiTop, guiLeft + WIDTH, guiTop + HEIGHT, 0xC0101010, 0xC0101010);
        graphics.fill(guiLeft - 1, guiTop - 1, guiLeft + WIDTH + 1, guiTop, 0xFF555555);
        graphics.fill(guiLeft - 1, guiTop + HEIGHT, guiLeft + WIDTH + 1, guiTop + HEIGHT + 1, 0xFF555555);
        graphics.fill(guiLeft - 1, guiTop, guiLeft, guiTop + HEIGHT, 0xFF555555);
        graphics.fill(guiLeft + WIDTH, guiTop, guiLeft + WIDTH + 1, guiTop + HEIGHT, 0xFF555555);
        graphics.fill(guiLeft, guiTop, guiLeft + WIDTH, guiTop + 20, 0x80000000);

        graphics.drawCenteredString(font, title, width / 2, guiTop - 12, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);

        int yOffset = guiTop + 28;
        switch (selectedTab) {
            case 0: // Market
                for (MarketItemData data : marketItems) {
                    ItemStack stack = new ItemStack(data.getItem());
                    graphics.renderItem(stack, guiLeft + 8, yOffset);
                    graphics.drawString(font, stack.getHoverName(), guiLeft + 28, yOffset + 5, 0xFFFFFF);
                    graphics.drawString(font, "Buy: " + data.getCurrentSellPrice() + "CC", guiLeft + 120, yOffset + 5, 0x00FF00);
                    graphics.drawString(font, "Sell: " + data.getCurrentBuyPrice() + "CC", guiLeft + 190, yOffset + 5, 0xFF0000);
                    yOffset += 20;
                }
                break;
            case 1: // Black Market
                graphics.drawString(font, "Daily Offers (PC):", guiLeft + 8, yOffset, 0xFFAA00);
                yOffset += 12;
                for (BlackMarket.BlackMarketOffer offer : BlackMarket.DAILY_OFFERS.values()) {
                    graphics.renderItem(offer.item, guiLeft + 8, yOffset);
                    graphics.drawString(font, offer.item.getHoverName(), guiLeft + 28, yOffset + 5, 0xFFFFFF);
                    graphics.drawString(font, "Price: " + offer.priceInPC + " PC", guiLeft + 120, yOffset + 5, 0xFFFF55);
                    graphics.drawString(font, "Stock: " + offer.stock, guiLeft + 190, yOffset + 5, 0xAAAAAA);
                    yOffset += 20;
                }
                break;
            case 2: // Recycle
                graphics.drawString(font, "Recycle gives 60% of market buy price.", guiLeft + 8, yOffset, 0xCCCCCC);
                break;
            case 3: // Bank
                graphics.drawString(font, "Your deposit: ? CC", guiLeft + 8, yOffset, 0xCCCCCC);
                graphics.drawString(font, "Daily interest: based on tier", guiLeft + 8, yOffset + 12, 0xCCCCCC);
                break;
            case 4: // Lottery
                graphics.drawString(font, "Bet and test your luck!", guiLeft + 8, yOffset, 0xCCCCCC);
                break;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
