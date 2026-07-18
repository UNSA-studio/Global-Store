package com.unsa.gs.globalstore.network;

import com.unsa.gs.globalstore.GlobalStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = GlobalStore.MODID, bus = EventBusSubscriber.Bus.MOD)
public record MarketSyncPacket(/* 市场数据，待序列化 */) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MarketSyncPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(GlobalStore.MODID, "market_sync"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {}, // 实现序列化
            buf -> new MarketSyncPacket()
    );

    public static void handle(MarketSyncPacket packet, IPayloadContext context) {
        // 客户端更新市场UI
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(GlobalStore.MODID).playToClient(TYPE, STREAM_CODEC, MarketSyncPacket::handle);
    }
}
