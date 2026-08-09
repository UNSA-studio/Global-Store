package com.unsa.gs.globalstore.network;

import com.unsa.gs.globalstore.GlobalStore;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * 服务端→客户端结果包，用于在GUI显示操作结果。
 */
@EventBusSubscriber(modid = GlobalStore.MODID)
public record GuiResultPacket(String message) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GuiResultPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(GlobalStore.MODID, "gui_result"));

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final StreamCodec<RegistryFriendlyByteBuf, GuiResultPacket> STREAM_CODEC =
        StreamCodec.of(
            (buf, p) -> buf.writeUtf(p.message),
            buf -> new GuiResultPacket(buf.readUtf())
        );

    public static void handle(GuiResultPacket p, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.screen instanceof com.unsa.gs.globalstore.gui.MarketScreen ms)
                ms.setStatus(p.message);
        });
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(GlobalStore.MODID)
            .playToClient(TYPE, STREAM_CODEC, GuiResultPacket::handle);
    }
}