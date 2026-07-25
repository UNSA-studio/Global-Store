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
import net.minecraft.resources.ResourceLocation;
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

        // 选项卡按钮（原版按钮样式）
        for (int i = 0; i < 5; i++) {
            final int tabIndex = i;
            addRenderableWidget(Button.builder(Component.literal(TAB_NAMES[i]), btn -> {
                selectedTab = tabIndex;
                rebuildWidgets();
            }).pos(guiLeft + 10 + i * 47, guiTop + 8).size(45, 16).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 更美观的深色面板，金色边框
        graphics.fillGradient(guiLeft, guiTop, guiLeft + WIDTH, guiTop + HEIGHT, 0xC0101010, 0xD0101010);
        // 外边框（金色）
        drawBorder(graphics, guiLeft, guiTop, WIDTH, HEIGHT, 0xFFB8860B); // DarkGoldenRod

        graphics.drawCenteredString(font, title, width / 2, guiTop - 10, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);

        int yOffset = guiTop + 30;
        switch (selectedTab) {
            case 0 -> {
                for (MarketItemData data : marketItems) {
                    ItemStack stack = new ItemStack(data.getItem());
                    graphics.renderItem(stack, guiLeft + 8, yOffset);
                    graphics.drawString(font, stack.getHoverName(), guiLeft + 28, yOffset + 5, 0xFFFFFF);
                    graphics.drawString(font, "Buy: " + data.getCurrentSellPrice() + " CC", guiLeft + 120, yOffset + 5, 0x00FF00);
                    graphics.drawString(font, "Sell: " + data.getCurrentBuyPrice() + " CC", guiLeft + 190, yOffset + 5, 0xFF0000);
                    yOffset += 20;
                }
            }
            case 1 -> {
                graphics.drawString(font, "Black Market (PC):", guiLeft + 8, yOffset, 0xFFAA00);
                yOffset += 14;
                for (BlackMarket.BlackMarketOffer offer : BlackMarket.DAILY_OFFERS.values()) {
                    graphics.renderItem(offer.item, guiLeft + 8, yOffset);
                    graphics.drawString(font, offer.item.getHoverName(), guiLeft + 28, yOffset + 5, 0xFFFFFF);
                    graphics.drawString(font, "Price: " + offer.priceInPC + " PC", guiLeft + 120, yOffset + 5, 0xFFFF55);
                    graphics.drawString(font, "Stock: " + offer.stock, guiLeft + 190, yOffset + 5, 0xAAAAAA);
                    yOffset += 20;
                }
            }
            case 2 -> {
                graphics.drawString(font, "Recycle (60% of market buy price):", guiLeft + 8, yOffset, 0xCCCCCC);
                yOffset += 12;
                for (MarketItemData data : marketItems) {
                    long recyclePrice = (long)(data.getCurrentBuyPrice() * 0.6);
                    graphics.drawString(font, data.getItem().getDescription().getString() + ": " + recyclePrice + " CC", guiLeft + 8, yOffset, 0xAAAAAA);
                    yOffset += 12;
                }
            }
            case 3 -> {
                long deposit = BankManager.getDeposit(Minecraft.getInstance().player.getUUID());
                graphics.drawString(font, "Your deposit: " + deposit + " CC", guiLeft + 8, yOffset, 0xCCCCCC);
                graphics.drawString(font, "Daily interest: " + String.format("%.2f%%", BankManager.getInterestRate(deposit)*100), guiLeft + 8, yOffset + 12, 0xCCCCCC);
            }
            case 4 -> {
                graphics.drawString(font, "Lottery - Spin to win!", guiLeft + 8, yOffset, 0xCCCCCC);
                // 按钮已在 init 中添加
            }
        }
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color); // top
        graphics.fill(x, y + h - 1, x + w, y + h, color); // bottom
        graphics.fill(x, y, x + 1, y + h, color); // left
        graphics.fill(x + w - 1, y, x + w, y + h, color); // right
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
