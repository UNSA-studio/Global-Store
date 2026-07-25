package com.unsa.gs.globalstore.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import com.unsa.gs.globalstore.GlobalStore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = GlobalStore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static Map<String, ModConfigSpec.ConfigValue<Long>> minPrices = new HashMap<>();
    public static Map<String, ModConfigSpec.ConfigValue<Long>> maxPrices = new HashMap<>();

    static {
        BUILDER.comment("Global Store item price configurations").push("prices");
        for (String item : new String[]{"diamond", "emerald", "iron_ingot", "gold_ingot", "netherite_ingot", "ender_pearl", "blaze_rod", "slime_ball", "obsidian", "enchanting_table", "anvil", "enchanted_book", "book", "coal", "redstone", "lapis_lazuli", "quartz"}) {
            minPrices.put(item, BUILDER.defineInRange("min_" + item, getDefaultMin(item), 1, Long.MAX_VALUE));
            maxPrices.put(item, BUILDER.defineInRange("max_" + item, getDefaultMax(item), 1, Long.MAX_VALUE));
        }
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private static long getDefaultMin(String item) {
        return switch (item) {
            case "diamond" -> 50;
            case "emerald" -> 5;
            case "iron_ingot" -> 2;
            case "gold_ingot" -> 5;
            case "netherite_ingot" -> 500;
            default -> 1;
        };
    }

    private static long getDefaultMax(String item) {
        return switch (item) {
            case "diamond" -> 200;
            case "emerald" -> 30;
            case "iron_ingot" -> 10;
            case "gold_ingot" -> 20;
            case "netherite_ingot" -> 2000;
            default -> 10;
        };
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            // 配置已加载
        }
    }
}
