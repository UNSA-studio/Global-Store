package com.unsa.gs.globalstore.network;

import com.unsa.gs.globalstore.GlobalStore;
import com.unsa.gs.globalstore.capability.PlayerAccount;
import com.unsa.gs.globalstore.core.bank.BankManager;
import com.unsa.gs.globalstore.core.lottery.LotterySystem;
import com.unsa.gs.globalstore.core.market.BlackMarket;
import com.unsa.gs.globalstore.core.market.GlobalMarket;
import com.unsa.gs.globalstore.core.stock.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * GUI操作包。action: 0=彩票 1=信用分 2=合格证明
 * 3=市场买入 4=市场卖出 5=回收 6=黑市购买 7=银行存款 8=银行取款 9=股票买入 10=股票卖出 11=铸币
 */
@EventBusSubscriber(modid = GlobalStore.MODID)
public record GuiActionPacket(int action, long param, String itemId, int amount) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GuiActionPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(GlobalStore.MODID, "gui_action"));

    public GuiActionPacket(int action, long param) { this(action, param, "", 0); }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final StreamCodec<RegistryFriendlyByteBuf, GuiActionPacket> STREAM_CODEC =
        StreamCodec.of(
            (buf, p) -> { buf.writeVarInt(p.action); buf.writeVarLong(p.param);
                buf.writeUtf(p.itemId); buf.writeVarInt(p.amount); },
            buf -> new GuiActionPacket(buf.readVarInt(), buf.readVarLong(),
                buf.readUtf(), buf.readVarInt())
        );

    public static void handle(GuiActionPacket p, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            PlayerAccount account = player.getData(
                com.unsa.gs.globalstore.capability.AccountCapability.PLAYER_ACCOUNT.get());
            if (account == null) return;
            String msg = "";
            Item item = p.itemId.isEmpty() ? null :
                BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(p.itemId));

            switch (p.action) {
                case 0 -> { // 彩票
                    if (account.getBalance() >= 10) {
                        account.subtractBalance(10);
                        long r = LotterySystem.roll(player, 10);
                        account.addBalance(10 + r);
                        msg = "彩票结果: " + (r > 0 ? "+" : "") + r + " CC";
                    } else msg = "余额不足10 CC";
                }
                case 1 -> { // 信用分恢复
                    if (CreditScore.recoverScore(player, (int)p.param))
                        msg = "信用分恢复" + p.param + "点！当前: " + CreditScore.getScore(player);
                    else msg = "余额不足或已达上限";
                }
                case 2 -> { // 合格证明
                    if (PlayerCompany.isCertified(player)) msg = "已获得合格证明！";
                    else if (PlayerCompany.requestCertification(player))
                        msg = "合格证明申请成功！花费10000 CC";
                    else msg = "条件不足：需信用分≥90 且余额≥10000 CC";
                }
                case 3 -> { // 市场买入
                    if (item == null) msg = "无效物品";
                    else msg = GlobalMarket.playerBuy(player, item, Math.max(1, p.amount));
                    if (msg == null) msg = "买入成功: " + p.amount + "x " + item.getDescription().getString();
                }
                case 4 -> { // 市场卖出
                    if (item == null) msg = "无效物品";
                    else msg = GlobalMarket.playerSell(player, item, Math.max(1, p.amount));
                    if (msg == null) msg = "卖出成功: " + p.amount + "x " + item.getDescription().getString();
                }
                case 5 -> { // 回收
                    if (item == null) msg = "无效物品";
                    else msg = GlobalMarket.playerRecycle(player, item, Math.max(1, p.amount));
                    if (msg == null) msg = "回收成功: " + p.amount + "x " + item.getDescription().getString();
                }
                case 6 -> { // 黑市购买
                    if (item == null) msg = "无效物品";
                    else {
                        var offer = BlackMarket.DAILY_OFFERS.stream()
                            .filter(o -> o.item.getItem() == item).findFirst().orElse(null);
                        if (offer == null) msg = "该物品今日不在黑市";
                        else if (BlackMarket.playerBuy(player, item, offer.priceInCC))
                            msg = "黑市购买成功: " + item.getDescription().getString();
                        else msg = "购买失败: 已达限量或余额不足";
                    }
                }
                case 7 -> { // 银行存款
                    long dep = Math.max(1, p.param);
                    if (account.getBalance() >= dep) {
                        BankManager.deposit(player, dep);
                        msg = "存入 " + dep + " CC";
                    } else msg = "余额不足";
                }
                case 8 -> { // 银行取款
                    long wd = Math.max(1, p.param);
                    if (BankManager.getDeposit(player.getUUID()) >= wd) {
                        BankManager.withdraw(player, wd);
                        msg = "取出 " + wd + " CC";
                    } else msg = "存款不足";
                }
                case 9 -> { // 股票买入
                    StockCompany sc = StockMarket.COMPANIES.get(p.itemId);
                    if (sc == null) msg = "公司不存在";
                    else if (sc.buyShares(player, Math.max(1, p.amount)))
                        msg = "购买 " + p.amount + " 股 " + sc.name + " @ " + sc.getPrice() + " CC";
                    else msg = "购买失败: 余额不足或股票不足";
                }
                case 10 -> { // 股票卖出
                    StockCompany sc = StockMarket.COMPANIES.get(p.itemId);
                    if (sc == null) msg = "公司不存在";
                    else if (sc.sellShares(player, Math.max(1, p.amount)))
                        msg = "卖出 " + p.amount + " 股 " + sc.name + " @ " + sc.getPrice() + " CC";
                    else msg = "卖出失败: 持股不足";
                }
                case 11 -> // 铸币
                    msg = BankManager.mintCoins(player, Math.max(1, p.param));
            }
            if (!msg.isEmpty())
                player.connection.send(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(new GuiResultPacket(msg)));
        });
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(GlobalStore.MODID)
            .playToServer(TYPE, STREAM_CODEC, GuiActionPacket::handle);
    }
}
