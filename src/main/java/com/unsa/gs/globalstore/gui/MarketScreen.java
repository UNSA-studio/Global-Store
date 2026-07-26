package com.unsa.gs.globalstore.gui;

import com.unsa.gs.globalstore.core.market.*;
import com.unsa.gs.globalstore.core.bank.BankManager;
import com.unsa.gs.globalstore.core.lottery.LotterySystem;
import com.unsa.gs.globalstore.core.stock.StockMarket;
import com.unsa.gs.globalstore.core.stock.StockCompany;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MarketScreen extends Screen {
    private static final int WIDTH = 360;
    private static final int HEIGHT = 260;
    private static final String[] TAB_NAMES = {"市场", "黑市", "回收", "银行", "彩票", "股票"};
    private int guiLeft, guiTop;
    private int selectedTab = 0;
    private double scrollOffset = 0;
    private boolean isDragging = false;
    private double dragStartY;
    private double startScroll;
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

        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int tabIndex = i;
            addRenderableWidget(Button.builder(Component.literal(TAB_NAMES[i]), btn -> {
                selectedTab = tabIndex;
                scrollOffset = 0;
            }).pos(guiLeft + 6, guiTop + 20 + i * 18).size(55, 16).build());
        }

        if (selectedTab == 4) {
            addRenderableWidget(Button.builder(Component.literal("旋转 (10CC)"), btn -> {
                long result = LotterySystem.roll(10);
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("彩票: " + result + " CC"));
            }).pos(guiLeft + 250, guiTop + 225).size(90, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(guiLeft, guiTop, guiLeft + WIDTH, guiTop + HEIGHT, 0xE0101010, 0xE0101010);
        drawBorder(graphics, guiLeft, guiTop, WIDTH, HEIGHT, 0xFFFFFFFF);
        graphics.drawCenteredString(font, title, width / 2, guiTop - 10, 0xFFFFFF);

        int contentLeft = guiLeft + 68;
        int contentTop = guiTop + 18;
        int contentWidth = 280;
        int contentHeight = 230;

        graphics.enableScissor(contentLeft, contentTop, contentLeft + contentWidth, contentTop + contentHeight);
        int renderY = contentTop + (int) -scrollOffset;
        switch (selectedTab) {
            case 0,1,2,3,4 -> renderLegacyTabs(graphics, contentLeft, renderY, contentWidth);
            case 5 -> renderStockTab(graphics, contentLeft, renderY, contentWidth);
        }
        graphics.disableScissor();

        // 滚动条
        int maxScroll = Math.max(0, (selectedTab==5 ? StockMarket.COMPANIES.size()*36 : marketItems.size()*38) - contentHeight);
        if (maxScroll > 0) {
            int scrollBarHeight = Math.max(10, (int)((double)contentHeight / (selectedTab==5 ? StockMarket.COMPANIES.size()*36 : marketItems.size()*38) * contentHeight));
            int scrollBarY = contentTop + (int)((scrollOffset / maxScroll) * (contentHeight - scrollBarHeight));
            graphics.fill(contentLeft + contentWidth - 4, contentTop, contentLeft + contentWidth, contentTop + contentHeight, 0xAA000000);
            graphics.fill(contentLeft + contentWidth - 4, scrollBarY, contentLeft + contentWidth, scrollBarY + scrollBarHeight, 0xFFFFFFFF);
        }

        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderLegacyTabs(GuiGraphics graphics, int x, int y, int width) {
        int renderY = y;
        if (selectedTab == 0) {
            for (MarketItemData data : marketItems) {
                drawItemCard(graphics, x + 2, renderY, width - 12, 36, data);
                renderY += 38;
            }
        } else if (selectedTab == 1) {
            graphics.drawString(font, "黑市 (PC)", x + 4, renderY + 4, 0xFFAA00);
            renderY += 18;
            for (BlackMarket.BlackMarketOffer offer : BlackMarket.DAILY_OFFERS.values()) {
                graphics.renderItem(offer.item, x + 4, renderY + 2);
                graphics.drawString(font, offer.item.getHoverName(), x + 24, renderY + 6, 0xFFFFFF);
                graphics.drawString(font, "价格: " + offer.priceInPC + " PC", x + 120, renderY + 6, 0xFFFF55);
                graphics.drawString(font, "库存: " + offer.stock, x + 180, renderY + 6, 0xAAAAAA);
                renderY += 20;
            }
        } else if (selectedTab == 2) {
            graphics.drawString(font, "回收 (60%市场买入价)", x + 4, renderY + 4, 0xCCCCCC);
            renderY += 16;
            for (MarketItemData data : marketItems) {
                long recyclePrice = (long)(data.getCurrentBuyPrice() * 0.6);
                graphics.drawString(font, data.getItem().getDescription().getString() + ": " + recyclePrice + " CC", x + 4, renderY + 4, 0xAAAAAA);
                renderY += 14;
            }
        } else if (selectedTab == 3) {
            long deposit = BankManager.getDeposit(Minecraft.getInstance().player.getUUID());
            graphics.drawString(font, "存款: " + deposit + " CC", x + 4, renderY + 4, 0xCCCCCC);
            graphics.drawString(font, "日利率: " + String.format("%.2f%%", BankManager.getInterestRate(deposit)*100), x + 4, renderY + 20, 0xCCCCCC);
        } else if (selectedTab == 4) {
            graphics.drawString(font, "彩票 - 来试试手气！", x + 4, renderY + 4, 0xCCCCCC);
        }
    }

    private void renderStockTab(GuiGraphics graphics, int x, int y, int width) {
        int renderY = y;
        for (StockCompany company : StockMarket.COMPANIES.values()) {
            graphics.fill(x + 2, renderY, x + width - 12, renderY + 34, 0x80000000);
            graphics.drawString(font, company.name, x + 6, renderY + 4, 0xFFFFFF);
            graphics.drawString(font, "股价: " + company.getPrice() + " CC", x + 6, renderY + 16, 0x00FF00);
            graphics.drawString(font, "可购: " + company.getAvailableShares(), x + 140, renderY + 16, 0xAAAAAA);
            renderY += 36;
        }
    }

    private void drawItemCard(GuiGraphics graphics, int x, int y, int width, int height, MarketItemData data) {
        graphics.fill(x, y, x + width, y + height, 0x80000000);
        ItemStack stack = new ItemStack(data.getItem());
        graphics.renderItem(stack, x + 3, y + 3);
        graphics.drawString(font, stack.getHoverName(), x + 22, y + 4, 0xFFFFFF);
        graphics.drawString(font, "库存:" + data.getStock(), x + 22, y + 16, 0xAAAAAA);
        graphics.drawString(font, "买入:" + data.getCurrentSellPrice() + "CC", x + 100, y + 4, 0x00FF00);
        graphics.drawString(font, "卖出:" + data.getCurrentBuyPrice() + "CC", x + 100, y + 16, 0xFF5555);
        long prev = data.getPreviousPrice();
        String trend = data.getCurrentSellPrice() > prev ? "↑" : (data.getCurrentSellPrice() < prev ? "↓" : "→");
        graphics.drawString(font, trend, x + 180, y + 10, 0xFFFF00);
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int contentHeight = 230;
        int totalItems = selectedTab==5 ? StockMarket.COMPANIES.size()*36 : marketItems.size()*38;
        double maxScroll = Math.max(0, totalItems - contentHeight);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * 10));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int contentLeft = guiLeft + 68;
        int contentTop = guiTop + 18;
        int contentWidth = 280;
        int contentHeight = 230;
        int scrollBarX = contentLeft + contentWidth - 4;
        if (mouseX >= scrollBarX && mouseX <= scrollBarX + 4 && mouseY >= contentTop && mouseY <= contentTop + contentHeight) {
            isDragging = true;
            dragStartY = mouseY;
            startScroll = scrollOffset;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            int contentHeight = 230;
            int totalItems = selectedTab==5 ? StockMarket.COMPANIES.size()*36 : marketItems.size()*38;
            double maxScroll = Math.max(0, totalItems - contentHeight);
            double deltaY = mouseY - dragStartY;
            double fraction = deltaY / contentHeight;
            scrollOffset = Math.max(0, Math.min(maxScroll, startScroll + fraction * maxScroll));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
