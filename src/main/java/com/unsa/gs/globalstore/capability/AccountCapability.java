package com.unsa.gs.globalstore.capability;

import com.unsa.gs.globalstore.GlobalStore;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.attachment.AttachmentRegistry;

import java.util.function.Supplier;

@EventBusSubscriber(modid = GlobalStore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class AccountCapability {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GlobalStore.MODID);

    public static final Supplier<AttachmentType<PlayerAccount>> PLAYER_ACCOUNT = ATTACHMENTS.register("player_account",
            () -> AttachmentType.builder(PlayerAccount::new).serialize(PlayerAccount.CODEC).build());

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // 已通过Attachment系统自动附加，无需额外注册
    }
}
