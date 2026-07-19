package com.unsa.gs.globalstore.network;

import com.unsa.gs.globalstore.GlobalStore;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = GlobalStore.MODID)
public record MarketSyncPacket(/* data */) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MarketSyncPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(GlobalStore.MODID, "market_sync"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, MarketSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new MarketSyncPacket()
    );

    public static void handle(MarketSyncPacket packet, IPayloadContext context) {
        // 客户端处理
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(GlobalStore.MODID).playToClient(TYPE, STREAM_CODEC, MarketSyncPacket::handle);
    }
}
