package com.unsa.gs.globalstore.network;

import com.unsa.gs.globalstore.GlobalStore;
import com.unsa.gs.globalstore.capability.PlayerAccount;
import com.unsa.gs.globalstore.core.lottery.LotterySystem;
import com.unsa.gs.globalstore.core.stock.CreditScore;
import com.unsa.gs.globalstore.core.stock.PlayerCompany;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * GUI操作网络包 — 客户端向服务端发送操作请求。
 * action: 0=彩票, 1=恢复信用分, 2=申请合格证明
 */
@EventBusSubscriber(modid = GlobalStore.MODID)
public record GuiActionPacket(int action, int param) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GuiActionPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(GlobalStore.MODID, "gui_action"));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final StreamCodec<RegistryFriendlyByteBuf, GuiActionPacket> STREAM_CODEC =
        StreamCodec.of(
            (buf, packet) -> { buf.writeVarInt(packet.action); buf.writeVarInt(packet.param); },
            buf -> new GuiActionPacket(buf.readVarInt(), buf.readVarInt())
        );

    public static void handle(GuiActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            PlayerAccount account = player.getData(
                com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
            if (account == null) return;
            String msg = "";

            switch (packet.action) {
                case 0 -> { // 彩票
                    if (account.getBalance() >= 10) {
                        account.subtractBalance(10);
                        long result = LotterySystem.roll(player, 10);
                        account.addBalance(10 + result);
                        msg = "彩票结果: " + (result > 0 ? "+" : "") + result + " CC";
                    } else {
                        msg = "余额不足10 CC";
                    }
                }
                case 1 -> { // 恢复信用分
                    if (CreditScore.recoverScore(player, packet.param)) {
                        msg = "信用分恢复" + packet.param + "点！当前: " + CreditScore.getScore(player);
                    } else {
                        msg = "余额不足或已达上限";
                    }
                }
                case 2 -> { // 申请合格证明
                    if (PlayerCompany.isCertified(player)) {
                        msg = "已获得合格证明！";
                    } else if (PlayerCompany.requestCertification(player)) {
                        msg = "合格证明申请成功！花费10000 CC";
                    } else {
                        msg = "条件不足：需信用分≥90 且余额≥10000 CC";
                    }
                }
            }
            if (!msg.isEmpty()) {
                player.sendSystemMessage(Component.literal("[GlobalStore] " + msg));
            }
        });
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(GlobalStore.MODID)
            .playToServer(TYPE, STREAM_CODEC, GuiActionPacket::handle);
    }
}
