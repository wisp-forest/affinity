package io.wispforest.affinity.component;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class EntityFlagComponent implements Component, AutoSyncedComponent {

    // @formatter:off
    public static final int NO_DROPS                           = 0b00000001;
    public static final int ITEM_GLOW                          = 0b00000010;
    public static final int SHOT_BY_AZALEA_BOW                 = 0b00000100;
    public static final int SPAWNED_BY_BREWING_CAULDRON        = 0b00001000;
    // @formatter:on

    private int flags = 0;

    public void setFlag(int flag) {
        flags |= flag;
    }

    public void unsetFlag(int flag) {
        flags &= ~flag;
    }

    public void clearFlags() {
        flags = 0;
    }

    public boolean hasFlag(int flag) {
        return (flags & flag) != 0;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        flags = tag.getInt("Flags");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        tag.putInt("Flags", flags);
    }
}
