package com.unsa.gs.globalstore.gui;

import com.unsa.gs.globalstore.GlobalStore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MarketScreen extends Screen {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(GlobalStore.MODID, "textures/gui/market_background.png");
    private static final int WIDTH = 248;
    private static final int HEIGHT = 166;
    private static final String[] TAB_NAMES = {"Market", "Black Market", "Recycle", "Bank", "Lottery"};
    private int guiLeft, guiTop;
    private int selectedTab = 0; // 0=Market,1=BlackMarket,2=Recycle,3=Bank,4=Lottery

    public MarketScreen() {
        super(Component.translatable("gui.globalstore.market"));
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - WIDTH) / 2;
        this.guiTop = (this.height - HEIGHT) / 2;

        // 添加5个选项卡按钮
        for (int i = 0; i < 5; i++) {
            final int tabIndex = i;
            addRenderableWidget(Button.builder(Component.literal(TAB_NAMES[i]), btn -> {
                selectedTab = tabIndex;
                rebuildWidgets();
            }).pos(guiLeft + 8 + i * 46, guiTop + 6).size(42, 16).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 绘制原版容器背景（稍后会用材质，这里先用渐变模拟）
        graphics.fillGradient(guiLeft, guiTop, guiLeft + WIDTH, guiTop + HEIGHT, 0xC0101010, 0xC0101010);
        graphics.fill(guiLeft - 1, guiTop - 1, guiLeft + WIDTH + 1, guiTop, 0xFF555555);
        graphics.fill(guiLeft - 1, guiTop + HEIGHT, guiLeft + WIDTH + 1, guiTop + HEIGHT + 1, 0xFF555555);
        graphics.fill(guiLeft - 1, guiTop, guiLeft, guiTop + HEIGHT, 0xFF555555);
        graphics.fill(guiLeft + WIDTH, guiTop, guiLeft + WIDTH + 1, guiTop + HEIGHT, 0xFF555555);
        graphics.fill(guiLeft, guiTop, guiLeft + WIDTH, guiTop + 20, 0x80000000); // 标签栏背景

        // 标题
        graphics.drawCenteredString(font, title, width / 2, guiTop - 12, 0xFFFFFF);

        // 渲染按钮
        super.render(graphics, mouseX, mouseY, partialTick);

        // 根据选中的选项卡绘制内容（暂空）
        String content = "Welcome to " + TAB_NAMES[selectedTab];
        graphics.drawString(font, content, guiLeft + 10, guiTop + 30, 0xCCCCCC);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
