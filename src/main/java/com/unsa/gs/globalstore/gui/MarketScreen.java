package com.unsa.gs.globalstore.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MarketScreen extends Screen {
    private static final int WIDTH = 256;
    private static final int HEIGHT = 200;
    private int guiLeft, guiTop;

    public MarketScreen() {
        super(Component.translatable("gui.globalstore.market"));
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - WIDTH) / 2;
        this.guiTop = (this.height - HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 完全不调用 super.render，避免模糊
        graphics.fillGradient(guiLeft, guiTop, guiLeft + WIDTH, guiTop + HEIGHT, 0xE0101010, 0xE0101010);
        graphics.fill(guiLeft - 1, guiTop - 1, guiLeft + WIDTH + 1, guiTop, 0xFFFFFFFF);
        graphics.fill(guiLeft - 1, guiTop + HEIGHT, guiLeft + WIDTH + 1, guiTop + HEIGHT + 1, 0xFFFFFFFF);
        graphics.fill(guiLeft - 1, guiTop, guiLeft, guiTop + HEIGHT, 0xFFFFFFFF);
        graphics.fill(guiLeft + WIDTH, guiTop, guiLeft + WIDTH + 1, guiTop + HEIGHT, 0xFFFFFFFF);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, guiTop + 8, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
