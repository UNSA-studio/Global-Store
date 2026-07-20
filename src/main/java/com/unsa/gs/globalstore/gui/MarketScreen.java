package com.unsa.gs.globalstore.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MarketScreen extends Screen {
    private static final int WIDTH = 256;
    private static final int HEIGHT = 200;

    public MarketScreen() {
        super(Component.translatable("gui.globalstore.market"));
    }

    @Override
    protected void init() {
        super.init();
        // 后续添加按钮
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 绘制半透明深色背景，不模糊
        int x = (this.width - WIDTH) / 2;
        int y = (this.height - HEIGHT) / 2;
        graphics.fillGradient(x, y, x + WIDTH, y + HEIGHT, 0xC0101010, 0xD0101010);
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0x40FFFFFF); // 边框高亮

        // 标题
        graphics.drawCenteredString(this.font, this.title, this.width / 2, y + 10, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
