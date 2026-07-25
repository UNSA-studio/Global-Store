package com.unsa.gs.globalstore.gui;

import com.unsa.gs.globalstore.GlobalStore;
import com.unsa.gs.globalstore.core.market.*;
import com.unsa.gs.globalstore.core.bank.BankManager;
import com.unsa.gs.globalstore.core.lottery.LotterySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MarketScreen extends Screen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");
    private static final int WIDTH = 310;
    private static final int HEIGHT = 220;
    private static final String[] TAB_NAMES = {"市场", "黑市", "回收", "银行", "彩票"};
    private int guiLeft, guiTop;
    private int selectedTab = 0;
    private double scrollOffset = 0;
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

        // 左侧选项卡按钮
        for (int i = 0; i < 5; i++) {
            final int tabIndex = i;
            addRenderableWidget(Button.builder(Component.literal(TAB_NAMES[i]), btn -> {
                selectedTab = tabIndex;
                scrollOffset = 0;
                rebuildWidgets();
            }).pos(guiLeft + 6, guiTop + 24 + i * 22).size(58, 20).build());
        }

        // 彩票按钮
        if (selectedTab == 4) {
            addRenderableWidget(Button.builder(Component.literal("旋转 (10CC)"), btn -> {
                long result = LotterySystem.roll(10);
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("彩票结果: " + result + " CC"));
            }).pos(guiLeft + 200, guiTop + 190).size(90, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = guiLeft;
        int y = guiTop;

        // 使用原版容器背景拉伸（避免方法报错，拉伸不影响外观）
        graphics.blit(BACKGROUND, x, y, 0, 0, WIDTH, HEIGHT, 176, 222);

        // 标题
        graphics.drawCenteredString(font, title, width / 2, y - 10, 0xFFFFFF);

        int contentLeft = guiLeft + 72;
        int contentTop = guiTop + 24;
        int contentWidth = 224;
        int contentHeight = 180;

        // 裁剪区域
        graphics.enableScissor(contentLeft, contentTop, contentLeft + contentWidth, contentTop + contentHeight);

        int renderY = contentTop + (int) -scrollOffset;
        switch (selectedTab) {
            case 0 -> {
                for (MarketItemData data : marketItems) {
                    ItemStack stack = new ItemStack(data.getItem());
                    graphics.renderItem(stack, contentLeft + 4, renderY + 2);
                    graphics.drawString(font, stack.getHoverName(), contentLeft + 24, renderY + 6, 0xFFFFFF);
                    graphics.drawString(font, "买入: " + data.getCurrentSellPrice() + " CC", contentLeft + 120, renderY + 6, 0x00FF00);
                    graphics.drawString(font, "卖出: " + data.getCurrentBuyPrice() + " CC", contentLeft + 180, renderY + 6, 0xFF0000);
                    renderY += 24;
                }
            }
            case 1 -> {
                graphics.drawString(font, "黑市 (PC)", contentLeft + 4, renderY + 4, 0xFFAA00);
                renderY += 18;
                for (BlackMarket.BlackMarketOffer offer : BlackMarket.DAILY_OFFERS.values()) {
                    graphics.renderItem(offer.item, contentLeft + 4, renderY + 2);
                    graphics.drawString(font, offer.item.getHoverName(), contentLeft + 24, renderY + 6, 0xFFFFFF);
                    graphics.drawString(font, "价格: " + offer.priceInPC + " PC", contentLeft + 120, renderY + 6, 0xFFFF55);
                    graphics.drawString(font, "库存: " + offer.stock, contentLeft + 180, renderY + 6, 0xAAAAAA);
                    renderY += 24;
                }
            }
            case 2 -> {
                graphics.drawString(font, "回收 (60%市场买入价)", contentLeft + 4, renderY + 4, 0xCCCCCC);
                renderY += 16;
                for (MarketItemData data : marketItems) {
                    long recyclePrice = (long)(data.getCurrentBuyPrice() * 0.6);
                    graphics.drawString(font, data.getItem().getDescription().getString() + ": " + recyclePrice + " CC", contentLeft + 4, renderY + 4, 0xAAAAAA);
                    renderY += 14;
                }
            }
            case 3 -> {
                long deposit = BankManager.getDeposit(Minecraft.getInstance().player.getUUID());
                graphics.drawString(font, "存款: " + deposit + " CC", contentLeft + 4, renderY + 4, 0xCCCCCC);
                graphics.drawString(font, "日利率: " + String.format("%.2f%%", BankManager.getInterestRate(deposit)*100), contentLeft + 4, renderY + 20, 0xCCCCCC);
            }
            case 4 -> {
                graphics.drawString(font, "彩票 - 来试试手气！", contentLeft + 4, renderY + 4, 0xCCCCCC);
            }
        }

        graphics.disableScissor();

        // 滚动条
        int maxScroll = Math.max(0, renderY - (contentTop + contentHeight));
        if (maxScroll > 0) {
            int scrollBarHeight = (int)((double)contentHeight / (renderY - contentTop) * contentHeight);
            int scrollBarY = contentTop + (int)((scrollOffset / maxScroll) * (contentHeight - scrollBarHeight));
            graphics.fill(contentLeft + contentWidth - 4, contentTop, contentLeft + contentWidth, contentTop + contentHeight, 0xAA000000);
            graphics.fill(contentLeft + contentWidth - 4, scrollBarY, contentLeft + contentWidth, scrollBarY + scrollBarHeight, 0xFFFFFFFF);
        }

        // 渲染按钮
        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int contentHeight = 180;
        int totalContentHeight = marketItems.size() * 24;
        double maxScroll = Math.max(0, totalContentHeight - contentHeight);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * 10));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
