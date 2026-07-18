package com.unsa.gs.globalstore.capability;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class PlayerAccount implements INBTSerializable<CompoundTag> {
    private long balance; // 以 CC 为单位存储

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void addBalance(long amount) {
        this.balance += amount;
    }

    public boolean subtractBalance(long amount) {
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("balance", balance);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        balance = nbt.getLong("balance");
    }
}
