package com.unsa.gs.globalstore.gui;

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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class MarketScreen extends Screen {
    private static final int WIDTH = 380, HEIGHT = 270;
    private static final String[] TAB_NAMES = {"市场", "黑市", "回收", "银行", "彩票", "股票"};
    private static final String[][] COIN_BUTTONS = {
        {"铸 CDMC", "11,100000000000"},
        {"铸 DMC", "11,1000000000"},
        {"铸 PC", "11,100000"},
        {"铸 TC", "11,100"},
        {"铸 CC", "11,1"},
    };
    private int guiLeft, guiTop, selectedTab;
    private double scrollOffset;
    private boolean isDragging;
    private double dragStartY, startScroll;
    private List<MarketItemData> marketItems = new ArrayList<>();
    private String statusMsg = "";
    private Item selectedItem = null;
    private String selectedStockSymbol = "";

    public MarketScreen() {
        super(Component.translatable("gui.globalstore.market"));
        marketItems = new ArrayList<>(GlobalMarket.MARKET_ITEMS.values());
    }

    public void setStatus(String msg) { this.statusMsg = msg; }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - WIDTH) / 2;
        this.guiTop = (this.height - HEIGHT) / 2;
        statusMsg = ""; selectedTab = 0; selectedItem = null; selectedStockSymbol = "";
        scrollOffset = 0;
        refreshButtons();
    }

    private String itemKey(Item item) {
        var key = BuiltInRegistries.ITEM.getKey(item);
        return key != null ? key.toString() : "";
    }

    private void refreshButtons() {
        clearWidgets();
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int ti = i;
            addRenderableWidget(Button.builder(Component.literal(TAB_NAMES[i]), btn -> {
                selectedTab = ti; scrollOffset = 0; statusMsg = "";
                selectedItem = null; selectedStockSymbol = "";
                refreshButtons();
            }).pos(guiLeft + 6, guiTop + 20 + i * 18).size(55, 16).build());
        }
        int rX = guiLeft + 255;

        switch (selectedTab) {
            case 0 -> {
                addRenderableWidget(Button.builder(Component.literal("买 1"), btn ->
                    sendTradeAction(3, 1)).pos(rX, guiTop + 200).size(50, 18).build());
                addRenderableWidget(Button.builder(Component.literal("买 10"), btn ->
                    sendTradeAction(3, 10)).pos(rX + 52, guiTop + 200).size(50, 18).build());
                addRenderableWidget(Button.builder(Component.literal("卖 1"), btn ->
                    sendTradeAction(4, 1)).pos(rX, guiTop + 220).size(50, 18).build());
                addRenderableWidget(Button.builder(Component.literal("卖 10"), btn ->
                    sendTradeAction(4, 10)).pos(rX + 52, guiTop + 220).size(50, 18).build());
            }
            case 1 ->
                addRenderableWidget(Button.builder(Component.literal("购买选中"), btn ->
                    sendAction(6)).pos(rX, guiTop + 200).size(105, 18).build());
            case 2 -> {
                addRenderableWidget(Button.builder(Component.literal("回收 1"), btn ->
                    sendTradeAction(5, 1)).pos(rX, guiTop + 200).size(50, 18).build());
                addRenderableWidget(Button.builder(Component.literal("回收 10"), btn ->
                    sendTradeAction(5, 10)).pos(rX + 52, guiTop + 200).size(50, 18).build());
            }
            case 3 -> {
                int by = guiTop + 168;
                for (String[] cb : COIN_BUTTONS) {
                    final String label = cb[0];
                    final String[] parts = cb[1].split(",");
                    final int action = Integer.parseInt(parts[0]);
                    final int amount = Integer.parseInt(parts[1]);
                    addRenderableWidget(Button.builder(Component.literal(label), bt ->
                        PacketDistributor.sendToServer(new GuiActionPacket(action, amount)))
                        .pos(rX, by).size(105, 16).build());
                    by += 18;
                }
                addRenderableWidget(Button.builder(Component.literal("取款 100"), btn ->
                    PacketDistributor.sendToServer(new GuiActionPacket(8, 100)))
                    .pos(rX, guiTop + 258).size(52, 16).build());
                addRenderableWidget(Button.builder(Component.literal("信+1"), btn ->
                    PacketDistributor.sendToServer(new GuiActionPacket(1, 1)))
                    .pos(rX + 54, guiTop + 258).size(30, 16).build());
                addRenderableWidget(Button.builder(Component.literal("证明"), btn ->
                    PacketDistributor.sendToServer(new GuiActionPacket(2, 0)))
                    .pos(rX + 86, guiTop + 258).size(34, 16).build());
            }
            case 4 ->
                addRenderableWidget(Button.builder(Component.literal("旋转 (10CC)"), btn ->
                    PacketDistributor.sendToServer(new GuiActionPacket(0, 0)))
                    .pos(rX, guiTop + 225).size(105, 20).build());
            case 5 -> {
                addRenderableWidget(Button.builder(Component.literal("买 10 股"), btn ->
                    sendStockAction(9, 10)).pos(rX, guiTop + 200).size(105, 18).build());
                addRenderableWidget(Button.builder(Component.literal("卖 10 股"), btn ->
                    sendStockAction(10, 10)).pos(rX, guiTop + 220).size(105, 18).build());
            }
        }
    }

    private void sendTradeAction(int action, int amount) {
        if (selectedItem == null) { statusMsg = "请先点击一个物品"; return; }
        statusMsg = "操作中...";
        PacketDistributor.sendToServer(new GuiActionPacket(action, 0, itemKey(selectedItem), amount));
    }

    private void sendAction(int action) {
        if (selectedItem == null) { statusMsg = "请先点击一个物品"; return; }
        statusMsg = "操作中...";
        PacketDistributor.sendToServer(new GuiActionPacket(action, 0, itemKey(selectedItem), 1));
    }

    private void sendStockAction(int action, int amount) {
        if (selectedStockSymbol.isEmpty()) { statusMsg = "请先点击一个公司"; return; }
        statusMsg = "操作中...";
        PacketDistributor.sendToServer(new GuiActionPacket(action, 0, selectedStockSymbol, amount));
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fillGradient(guiLeft, guiTop, guiLeft + WIDTH, guiTop + HEIGHT, 0xE0101010, 0xE0101010);
        drawBorder(g, guiLeft, guiTop, WIDTH, HEIGHT, 0xFFFFFFFF);
        g.drawCenteredString(font, title, width / 2, guiTop - 10, 0xFFFFFF);
        int cx = guiLeft + 68, cy = guiTop + 18, cw = 180, ch = 240;
        g.enableScissor(cx, cy, cx + cw, cy + ch);
        int ry = cy + (int)-scrollOffset;
        switch (selectedTab) {
            case 0 -> renderMarket(g, cx, ry, cw);
            case 1 -> renderBlackMarket(g, cx, ry, cw);
            case 2 -> renderRecycle(g, cx, ry, cw);
            case 3 -> renderBank(g, cx, ry, cw);
            case 4 -> renderLottery(g, cx, ry, cw);
            case 5 -> renderStock(g, cx, ry, cw);
        }
        g.disableScissor();
        if (!statusMsg.isEmpty())
            g.drawString(font, statusMsg, guiLeft + 68, guiTop + 255, 0xFFFF55);
        String sel = "";
        if (selectedItem != null)
            sel = "物品: " + new ItemStack(selectedItem).getHoverName().getString();
        else if (!selectedStockSymbol.isEmpty())
            sel = "股票: " + selectedStockSymbol;
        if (!sel.isEmpty())
            g.drawString(font, sel, guiLeft + 68, guiTop + 258, 0x00FF00);
        for (Renderable r : this.renderables) r.render(g, mx, my, pt);
    }

    private void renderMarket(GuiGraphics g, int x, int y, int w) {
        if (marketItems.isEmpty()) { g.drawString(font, "加载中...", x + 4, y + 4, 0xCCCCCC); return; }
        int ry = y;
        for (MarketItemData d : marketItems) {
            if (ry > y + 250) break;
            boolean sel = selectedItem == d.getItem();
            g.fill(x + 2, ry, x + w - 12, ry + 36, sel ? 0xFF444444 : 0x80000000);
            ItemStack st = new ItemStack(d.getItem());
            g.renderItem(st, x + 3, ry + 3);
            String nm = st.getHoverName().getString();
            if (nm.length() > 18) nm = nm.substring(0, 18);
            g.drawString(font, nm, x + 22, ry + 4, 0xFFFFFF);
            g.drawString(font, "库存:" + d.getStock(), x + 22, ry + 16, 0xAAAAAA);
            g.drawString(font, "买:" + d.getCurrentSellPrice() + " 卖:" + d.getCurrentBuyPrice(),
                x + 100, ry + 4, 0x00FF00);
            long prev = d.getPreviousPrice();
            String tr = d.getCurrentSellPrice() > prev ? "↑" : (d.getCurrentSellPrice() < prev ? "↓" : "→");
            g.drawString(font, tr, x + 100, ry + 16, 0xFFFF00);
            ry += 38;
        }
    }

    private void renderBlackMarket(GuiGraphics g, int x, int y, int w) {
        int ry = y;
        if (!BlackMarket.DAILY_SPECIALS.isEmpty()) {
            g.drawString(font, "=== 每日特价 ===", x + 4, ry, 0xFF5500); ry += 14;
            for (BlackMarket.DailySpecialOffer s : BlackMarket.DAILY_SPECIALS) {
                g.fill(x + 2, ry, x + w - 12, ry + 20, selectedItem == s.item.getItem() ? 0xFF444444 : 0x80000000);
                g.drawString(font, s.item.getHoverName().getString() + " " + s.price + "CC x" + s.remainingStock,
                    x + 4, ry + 4, 0xFFAA00);
                ry += 22;
            }
        }
        g.drawString(font, "=== 黑市 ===", x + 4, ry, 0xFFAA00); ry += 14;
        for (BlackMarket.BlackMarketOffer o : BlackMarket.DAILY_OFFERS) {
            g.fill(x + 2, ry, x + w - 12, ry + 22, selectedItem == o.item.getItem() ? 0xFF444444 : 0x80000000);
            g.renderItem(o.item, x + 4, ry + 2);
            g.drawString(font, o.item.getHoverName().getString() + " " + o.priceInCC + " CC",
                x + 24, ry + 6, 0xFF5555);
            ry += 24;
        }
    }

    private void renderRecycle(GuiGraphics g, int x, int y, int w) {
        g.drawString(font, "回收 (60%买入价)", x + 4, y + 4, 0xCCCCCC);
        int ry = y + 20;
        for (MarketItemData d : marketItems) {
            if (ry > y + 230) break;
            long pr = (long)(d.getCurrentBuyPrice() * 0.6);
            g.fill(x + 2, ry, x + w - 12, ry + 14, selectedItem == d.getItem() ? 0xFF444444 : 0x80000000);
            g.drawString(font, d.getItem().getDescription().getString() + ": " + pr + " CC", x + 4, ry + 4, 0xAAAAAA);
            ry += 16;
        }
    }

    private void renderBank(GuiGraphics g, int x, int y, int w) {
        Player p = Minecraft.getInstance().player;
        if (p == null) return;
        long dep = BankManager.getDeposit(p.getUUID());
        PlayerAccount ac = p.getData(com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
        long bal = ac != null ? ac.getBalance() : 0;
        int sc = CreditScore.getScore(p);
        boolean cert = PlayerCompany.isCertified(p);
        int ry = y;
        g.drawString(font, "余额: " + String.format("%,d", bal) + " CC", x + 4, ry, 0xFFFFFF); ry += 14;
        g.drawString(font, "存款: " + String.format("%,d", dep) + " CC (利率 " +
            String.format("%.2f%%", BankManager.getInterestRate(dep) * 100) + ")", x + 4, ry, 0xFFFFFF); ry += 14;
        g.drawString(font, "存款方式: 潜行+右键硬币", x + 4, ry, 0xAAAAAA); ry += 14;
        int scCol = sc >= 90 ? 0x00FF00 : (sc >= 30 ? 0xFFFF00 : 0xFF5555);
        g.drawString(font, "信用: " + sc + "/100", x + 4, ry, scCol); ry += 14;
        g.drawString(font, cert ? "已认证 ✓" : "未认证", x + 4, ry, cert ? 0x00FF00 : 0xFF5555);
    }

    private void renderLottery(GuiGraphics g, int x, int y, int w) {
        g.drawString(font, "10CC/注 | 10%翻倍 20%赢半", x + 4, y + 4, 0xCCCCCC);
        g.drawString(font, "20%不赚 20%赔半 30%全赔", x + 4, y + 16, 0xCCCCCC);
        g.drawString(font, "10次未中触发保底", x + 4, y + 30, 0xFFFF55);
    }

    private void renderStock(GuiGraphics g, int x, int y, int w) {
        if (StockMarket.COMPANIES.isEmpty()) { g.drawString(font, "加载中...", x + 4, y + 4, 0xCCCCCC); return; }
        int ry = y;
        Player p = Minecraft.getInstance().player;
        for (var e : StockMarket.COMPANIES.entrySet()) {
            StockCompany sc = e.getValue();
            boolean sel = e.getKey().equals(selectedStockSymbol);
            g.fill(x + 2, ry, x + w - 12, ry + 36, sel ? 0xFF444444 : 0x80000000);
            g.drawString(font, sc.name + " (" + e.getKey() + ")", x + 6, ry + 4, 0xFFFFFF);
            g.drawString(font, "股价: " + String.format("%,d", sc.getPrice()) + " CC  可购: " + sc.getAvailableShares(),
                x + 6, ry + 16, 0x00FF00);
            if (p != null)
                g.drawString(font, "持仓: " + sc.getPlayerShares(p.getUUID()) + " 股", x + 6, ry + 26, 0xAAAAAA);
            ry += 38;
        }
    }

    private void drawBorder(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c); g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c); g.fill(x + w - 1, y, x + w, y + h, c);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int cx = guiLeft + 68, cy = guiTop + 18, cw = 180, ch = 240;
        if (mx >= cx && mx <= cx + cw && my >= cy && my <= cy + ch) {
            double ry = my - cy + scrollOffset;
            selectedStockSymbol = ""; // 在非股票标签清除股票选中
            switch (selectedTab) {
                case 0 -> {
                    int idx = (int)(ry / 38);
                    if (idx >= 0 && idx < marketItems.size()) {
                        selectedItem = marketItems.get(idx).getItem();
                        statusMsg = "选中: " + new ItemStack(selectedItem).getHoverName().getString();
                        return true;
                    }
                }
                case 1 -> {
                    int idx = (int)((ry - (BlackMarket.DAILY_SPECIALS.isEmpty() ? 0 :
                        14 + BlackMarket.DAILY_SPECIALS.size() * 22)) / 24);
                    if (idx >= 0 && idx < BlackMarket.DAILY_OFFERS.size()) {
                        selectedItem = BlackMarket.DAILY_OFFERS.get(idx).item.getItem();
                        statusMsg = "选中: " + selectedItem.getDescription().getString();
                        return true;
                    }
                }
                case 2 -> {
                    int idx = (int)((ry - 20) / 16);
                    if (idx >= 0 && idx < marketItems.size()) {
                        selectedItem = marketItems.get(idx).getItem();
                        statusMsg = "选中: " + selectedItem.getDescription().getString();
                        return true;
                    }
                }
                case 5 -> {
                    selectedItem = null; // 清除物品选中
                    int idx = (int)(ry / 38);
                    int i = 0;
                    for (var e : StockMarket.COMPANIES.entrySet()) {
                        if (i == idx) {
                            selectedStockSymbol = e.getKey();
                            statusMsg = "选中: " + e.getValue().name + " (" + e.getKey() + ")";
                            return true;
                        }
                        i++;
                    }
                }
            }
        }
        int sbX = cx + cw - 4;
        if (mx >= sbX && mx <= sbX + 4 && my >= cy && my <= cy + ch) {
            isDragging = true; dragStartY = my; startScroll = scrollOffset; return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override public boolean mouseReleased(double mx, double my, int b) { isDragging = false; return super.mouseReleased(mx, my, b); }
    @Override public boolean mouseDragged(double mx, double my, int b, double dx, double dy) {
        if (isDragging) { int ch = 240; int ti = selectedTab == 5 ? StockMarket.COMPANIES.size() * 38 : marketItems.size() * 38;
            double ms = Math.max(0, ti - ch); scrollOffset = Math.max(0, Math.min(ms, startScroll + (my - dragStartY) / ch * ms)); return true; }
        return super.mouseDragged(mx, my, b, dx, dy);
    }
    @Override public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        int ch = 240; int ti = selectedTab == 5 ? StockMarket.COMPANIES.size() * 38 : marketItems.size() * 38;
        double ms = Math.max(0, ti - ch); scrollOffset = Math.max(0, Math.min(ms, scrollOffset - sy * 10)); return true;
    }
    @Override public boolean isPauseScreen() { return false; }
}
