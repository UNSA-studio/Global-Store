package com.unsa.gs.globalstore.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.unsa.gs.globalstore.GlobalStore;

public class MarketScreen extends Screen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(GlobalStore.MODID, "textures/gui/market_background.png");
    private int tabIndex = 0; // 0=Market,1=BlackMarket,2=Recycle,3=Bank,4=Lottery
    private static final String[] TAB_NAMES = {"Market", "Black Market", "Recycle", "Bank", "Lottery"};

    protected MarketScreen() {
        super(Component.translatable("gui.globalstore.market"));
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - 200) / 2;
        int y = (height - 200) / 2;
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal(TAB_NAMES[i]), btn -> {
                tabIndex = idx;
                rebuildWidgets();
            }).pos(x + 10 + i * 40, y + 5).size(35, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, (height - 200) / 2 - 10, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
