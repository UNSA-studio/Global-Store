package com.unsa.gs.globalstore.capability;

import com.unsa.gs.globalstore.GlobalStore;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.function.Supplier;

@EventBusSubscriber(modid = GlobalStore.MODID)
public class AccountCapability {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GlobalStore.MODID);

    public static final Supplier<AttachmentType<PlayerAccount>> PLAYER_ACCOUNT = ATTACHMENTS.register(
            "player_account",
            () -> AttachmentType.builder(PlayerAccount::new).serialize(PlayerAccount.CODEC).build()
    );

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Attachment 自动附加，无需在此注册
    }
}
