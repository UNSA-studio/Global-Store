package com.unsa.gs.globalstore.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import com.unsa.gs.globalstore.GlobalStore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = GlobalStore.MODID)
public class ModConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // 大宗交易阈值（>= 此数量触发价格波动和监控）
    public static ModConfigSpec.LongValue BULK_TRADE_THRESHOLD;

    // 恶意操纵检测窗口内最大允许交易次数
    public static ModConfigSpec.IntValue MANIPULATION_MAX_TRADES;

    // 基础冷却时间（分钟）
    public static ModConfigSpec.LongValue BASE_COOLDOWN_MINUTES;

    static {
        BUILDER.comment("Global Store — Market & Trade Monitor Config").push("market");

        BULK_TRADE_THRESHOLD = BUILDER
            .comment("单次交易数量 >= 此值视为大宗交易，触发价格波动")
            .defineInRange("bulkTradeThreshold", 32L, 1L, 10000L);

        MANIPULATION_MAX_TRADES = BUILDER
            .comment("5分钟内同物品大宗交易次数上限，超过即视为操纵")
            .defineInRange("manipulationMaxTrades", 5, 1, 100);

        BASE_COOLDOWN_MINUTES = BUILDER
            .comment("大宗交易后基础冷却时间（分钟），再犯叠加+10分钟，上限48小时")
            .defineInRange("baseCooldownMinutes", 5L, 1L, 60L);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void register() {
        ModList.get().getModContainerById(GlobalStore.MODID).ifPresent(container ->
            container.registerConfig(ModConfig.Type.COMMON, SPEC)
        );
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            // 配置已加载
        }
    }
}
