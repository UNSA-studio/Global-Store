package com.unsa.gs.globalstore.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final Lazy<KeyMapping> OPEN_MARKET_KEY = Lazy.of(() ->
            new KeyMapping(
                    "key.globalstore.open_market",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_G,
                    "key.categories.globalstore"
            )
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MARKET_KEY.get());
    }
}
