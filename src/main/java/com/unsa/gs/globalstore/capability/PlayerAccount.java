package com.unsa.gs.globalstore.capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class PlayerAccount implements INBTSerializable<CompoundTag> {
    private long balance;

    public static final Codec<PlayerAccount> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.fieldOf("balance").forGetter(a -> a.balance)
            ).apply(instance, PlayerAccount::new)
    );

    public PlayerAccount() {
        this(0);
    }

    public PlayerAccount(long balance) {
        this.balance = balance;
    }

    public long getBalance() { return balance; }
    public void setBalance(long balance) { this.balance = balance; }
    public void addBalance(long amount) { this.balance += amount; }
    public boolean subtractBalance(long amount) {
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    // NeoForge 1.21.1 新接口方法
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("balance", balance);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        balance = nbt.getLong("balance");
    }
}
