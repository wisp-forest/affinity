package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;

public class EntityFlagComponent implements Component, AutoSyncedComponent {

    public static final int NO_DROPS = 0x1;
    public static final int ITEM_GLOW = 0x2;

    private int flags = 0;

    public void setFlag(int flag) {
        flags |= flag;
    }

    public void clearFlags() {
        flags = 0;
    }

    public boolean hasFlag(int flag) {
        return (flags & flag) != 0;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        flags = tag.getInt("Flags");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("Flags", flags);
    }
}
