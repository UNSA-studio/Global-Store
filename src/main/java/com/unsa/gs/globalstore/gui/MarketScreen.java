package com.unsa.gs.globalstore.gui;

import com.unsa.gs.globalstore.GlobalStore;
import com.unsa.gs.globalstore.capability.PlayerAccount;
import com.unsa.gs.globalstore.core.bank.BankManager;
import com.unsa.gs.globalstore.core.market.*;
import com.unsa.gs.globalstore.core.stock.*;
import com.unsa.gs.globalstore.network.GuiActionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class MarketScreen extends Screen {
    private static final int WIDTH = 380;
    private static final int HEIGHT = 270;
    private static final String[] TAB_NAMES = {"市场", "黑市", "回收", "银行", "彩票", "股票"};
    private int guiLeft, guiTop;
    private int selectedTab = 0;
    private double scrollOffset = 0;
    private boolean isDragging = false;
    private double dragStartY;
    private double startScroll;
    private List<MarketItemData> marketItems = new ArrayList<>();
    private String bankStatusMessage = "";

    public MarketScreen() {
        super(Component.translatable("gui.globalstore.market"));
        marketItems = new ArrayList<>(GlobalMarket.MARKET_ITEMS.values());
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - WIDTH) / 2;
        this.guiTop = (this.height - HEIGHT) / 2;
        bankStatusMessage = "";

        refreshDynamicButtons();
    }

    private void refreshDynamicButtons() {
        // 清除动态按钮并重建：Screen.clearWidgets() 清除所有，然后重新加标签页
        clearWidgets();
        // 重新添加标签页按钮
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int tabIndex = i;
            addRenderableWidget(Button.builder(Component.literal(TAB_NAMES[i]), btn -> {
                selectedTab = tabIndex;
                scrollOffset = 0;
                bankStatusMessage = "";
                refreshDynamicButtons();
            }).pos(guiLeft + 6, guiTop + 20 + i * 18).size(55, 16).build());
        }

        if (selectedTab == 3) {
            // 银行标签页：信用分恢复 + 合格证明申请
            int btnX = guiLeft + 250;
            addRenderableWidget(Button.builder(Component.literal("恢复信用分(1点)"), btn -> {
                PacketDistributor.sendToServer(new GuiActionPacket(1, 1));
                bankStatusMessage = "已发送信用分恢复请求...";
            }).pos(btnX, guiTop + 200).size(110, 18).build());

            addRenderableWidget(Button.builder(Component.literal("恢复10点"), btn -> {
                PacketDistributor.sendToServer(new GuiActionPacket(1, 10));
                bankStatusMessage = "已发送信用分恢复请求...";
            }).pos(btnX, guiTop + 220).size(110, 18).build());

            addRenderableWidget(Button.builder(Component.literal("申请合格证明"), btn -> {
                PacketDistributor.sendToServer(new GuiActionPacket(2, 0));
                bankStatusMessage = "已发送合格证明申请...";
            }).pos(btnX, guiTop + 240).size(110, 18).build());
        }

        if (selectedTab == 4) {
            // 彩票
            addRenderableWidget(Button.builder(Component.literal("旋转 (10CC)"), btn -> {
                PacketDistributor.sendToServer(new GuiActionPacket(0, 0));
                bankStatusMessage = "已下注10 CC...";
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
        int contentWidth = 180; // 缩小给按钮留空间
        int contentHeight = 240;

        graphics.enableScissor(contentLeft, contentTop, contentLeft + contentWidth, contentTop + contentHeight);
        int renderY = contentTop + (int) -scrollOffset;
        switch (selectedTab) {
            case 0 -> renderMarketTab(graphics, contentLeft, renderY, contentWidth);
            case 1 -> renderBlackMarketTab(graphics, contentLeft, renderY, contentWidth);
            case 2 -> renderRecycleTab(graphics, contentLeft, renderY, contentWidth);
            case 3 -> renderBankTab(graphics, contentLeft, renderY, contentWidth);
            case 4 -> renderLotteryTab(graphics, contentLeft, renderY, contentWidth);
            case 5 -> renderStockTab(graphics, contentLeft, renderY, contentWidth);
        }
        graphics.disableScissor();

        // 状态消息
        if (!bankStatusMessage.isEmpty()) {
            graphics.drawString(font, bankStatusMessage, guiLeft + 68, guiTop + 255, 0xFFFF55);
        }

        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderMarketTab(GuiGraphics graphics, int x, int y, int width) {
        if (marketItems.isEmpty()) {
            graphics.drawString(font, "正在加载市场数据...", x + 4, y + 4, 0xCCCCCC);
            return;
        }
        // 搜索市场物品（仅显示可合成/自然生成的）
        int renderY = y;
        int count = 0;
        for (MarketItemData data : marketItems) {
            if (renderY > y + 250) break; // 性能限制
            drawItemCard(graphics, x + 2, renderY, width - 12, 36, data);
            renderY += 38;
            count++;
        }
    }

    private void renderBlackMarketTab(GuiGraphics graphics, int x, int y, int width) {
        int renderY = y;

        // 每日特价（先到先得）
        if (!BlackMarket.DAILY_SPECIALS.isEmpty()) {
            graphics.drawString(font, "=== 每日特价 (低于市场60%!) ===", x + 4, renderY, 0xFF5500);
            renderY += 14;
            int idx = 0;
            for (BlackMarket.DailySpecialOffer special : BlackMarket.DAILY_SPECIALS) {
                graphics.drawString(font, "[" + idx + "] " + special.item.getHoverName().getString(),
                    x + 4, renderY, 0xFFFFFF);
                graphics.drawString(font, "价格: " + special.price + " CC 库存: " + special.remainingStock,
                    x + 4, renderY + 12, 0xFFAA00);
                renderY += 26;
                idx++;
            }
            renderY += 8;
        }

        // 常规黑市商品
        graphics.drawString(font, "=== 黑市商品 (限量" + BlackMarket.PLAYER_BUY_LIMIT + "个/人) ===", x + 4, renderY, 0xFFAA00);
        renderY += 14;
        for (BlackMarket.BlackMarketOffer offer : BlackMarket.DAILY_OFFERS) {
            graphics.renderItem(offer.item, x + 4, renderY + 2);
            graphics.drawString(font, offer.item.getHoverName().getString(), x + 24, renderY + 6, 0xFFFFFF);
            graphics.drawString(font, "价格: " + offer.priceInCC + " CC", x + 24, renderY + 18, 0xFF5555);
            renderY += 30;
        }
    }

    private void renderRecycleTab(GuiGraphics graphics, int x, int y, int width) {
        graphics.drawString(font, "回收 (60%市场买入价)", x + 4, y + 4, 0xCCCCCC);
        int renderY = y + 20;
        for (MarketItemData data : marketItems) {
            long recyclePrice = (long)(data.getCurrentBuyPrice() * 0.6);
            String name = data.getItem().getDescription().getString();
            if (name.length() > 20) name = name.substring(0, 20);
            graphics.drawString(font, name + ": " + recyclePrice + " CC", x + 4, renderY, 0xAAAAAA);
            renderY += 14;
            if (renderY > y + 230) break;
        }
    }

    private void renderBankTab(GuiGraphics graphics, int x, int y, int width) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        long deposit = BankManager.getDeposit(player.getUUID());
        PlayerAccount account = player.getData(com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        long balance = account != null ? account.getBalance() : 0;
        int score = CreditScore.getScore(player);
        boolean certified = PlayerCompany.isCertified(player);

        int renderY = y;
        graphics.drawString(font, "=== 银行管理 ===", x + 4, renderY, 0xCCCCCC);
        renderY += 16;
        graphics.drawString(font, "账户余额: " + balance + " CC", x + 4, renderY, 0xFFFFFF);
        renderY += 14;
        graphics.drawString(font, "银行存款: " + deposit + " CC", x + 4, renderY, 0xFFFFFF);
        renderY += 14;
        graphics.drawString(font, "日利率: " + String.format("%.2f%%", BankManager.getInterestRate(deposit) * 100),
            x + 4, renderY, 0xFFFFFF);
        renderY += 16;

        graphics.drawString(font, "=== 信用管理 ===", x + 4, renderY, 0xCCCCCC);
        renderY += 16;
        int scoreColor = score >= 90 ? 0x00FF00 : (score >= 30 ? 0xFFFF00 : 0xFF5555);
        graphics.drawString(font, "信用分: " + score + "/100", x + 4, renderY, scoreColor);
        renderY += 14;
        graphics.drawString(font, "恢复费用: 1点 = 1000 CC", x + 4, renderY, 0xAAAAAA);
        renderY += 16;

        graphics.drawString(font, "=== 合格证明 ===", x + 4, renderY, 0xCCCCCC);
        renderY += 16;
        String certStatus = certified ? "已认证 ✓" : "未认证 (需信用分≥90 + 10000 CC)";
        int certColor = certified ? 0x00FF00 : 0xFF5555;
        graphics.drawString(font, certStatus, x + 4, renderY, certColor);
    }

    private void renderLotteryTab(GuiGraphics graphics, int x, int y, int width) {
        graphics.drawString(font, "彩票 — 来试试手气！", x + 4, y + 4, 0xCCCCCC);
        graphics.drawString(font, "10 CC 一注", x + 4, y + 18, 0xAAAAAA);
        graphics.drawString(font, "10%翻倍 | 20%赢50% | 20%不赚", x + 4, y + 34, 0xAAAAAA);
        graphics.drawString(font, "20%赔50% | 30%全赔", x + 4, y + 48, 0xAAAAAA);
        graphics.drawString(font, "保底: 连续10次未盈利触发小奖", x + 4, y + 64, 0xFFFF55);
    }

    private void renderStockTab(GuiGraphics graphics, int x, int y, int width) {
        int renderY = y;
        if (StockMarket.COMPANIES.isEmpty()) {
            graphics.drawString(font, "股票市场加载中...", x + 4, renderY + 4, 0xCCCCCC);
            return;
        }
        for (StockCompany company : StockMarket.COMPANIES.values()) {
            graphics.fill(x + 2, renderY, x + width - 12, renderY + 34, 0x80000000);
            graphics.drawString(font, company.name, x + 6, renderY + 4, 0xFFFFFF);
            graphics.drawString(font, "股价: " + company.getPrice() + " CC", x + 6, renderY + 16, 0x00FF00);
            graphics.drawString(font, "可购: " + company.getAvailableShares(), x + 6, renderY + 28, 0xAAAAAA);
            renderY += 36;
        }
    }

    private void drawItemCard(GuiGraphics graphics, int x, int y, int width, int height, MarketItemData data) {
        graphics.fill(x, y, x + width, y + height, 0x80000000);
        ItemStack stack = new ItemStack(data.getItem());
        graphics.renderItem(stack, x + 3, y + 3);
        String name = stack.getHoverName().getString();
        if (name.length() > 18) name = name.substring(0, 18);
        graphics.drawString(font, name, x + 22, y + 4, 0xFFFFFF);
        graphics.drawString(font, "库存:" + data.getStock(), x + 22, y + 16, 0xAAAAAA);
        graphics.drawString(font, "买入:" + data.getCurrentSellPrice() + "CC", x + 100, y + 4, 0x00FF00);
        graphics.drawString(font, "卖出:" + data.getCurrentBuyPrice() + "CC", x + 100, y + 16, 0xFF5555);
        long prev = data.getPreviousPrice();
        String trend = data.getCurrentSellPrice() > prev ? "↑" : (data.getCurrentSellPrice() < prev ? "↓" : "→");
        graphics.drawString(font, trend, x + 160, y + 10, 0xFFFF00);
        if (data.isModded()) {
            graphics.drawString(font, "(模组)", x + 155, y + 20, 0xAAAAAA);
        }
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int contentHeight = 240;
        int totalItems = selectedTab == 5 ? StockMarket.COMPANIES.size() * 36 : marketItems.size() * 38;
        double maxScroll = Math.max(0, totalItems - contentHeight);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * 10));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int contentLeft = guiLeft + 68;
        int contentTop = guiTop + 18;
        int contentWidth = 180;
        int contentHeight = 240;
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
            int contentHeight = 240;
            int totalItems = selectedTab == 5 ? StockMarket.COMPANIES.size() * 36 : marketItems.size() * 38;
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
