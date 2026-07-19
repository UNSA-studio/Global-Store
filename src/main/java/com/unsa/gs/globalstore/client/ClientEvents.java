package com.unsa.gs.globalstore.client;

import com.unsa.gs.globalstore.GlobalStore;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = GlobalStore.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        long window = mc.getWindow().getWindow();
        boolean gDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_G);
        boolean sDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_S);

        if (gDown && sDown && event.getAction() == GLFW.GLFW_PRESS) {
            mc.setScreen(new MarketScreen());
        }
    }
}
