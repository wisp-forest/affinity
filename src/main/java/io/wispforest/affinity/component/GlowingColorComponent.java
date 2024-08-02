package io.wispforest.affinity.component;

import io.wispforest.affinity.misc.potion.GlowingPotion;
import io.wispforest.endec.SerializationContext;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.DyeColor;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class GlowingColorComponent implements Component, AutoSyncedComponent {

    private final Entity provider;
    private DyeColor color = null;

    public GlowingColorComponent(Entity provider) {
        this.provider = provider;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        this.color = tag.get(GlowingPotion.COLOR_KEY);
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        tag.putIfNotNull(SerializationContext.empty(), GlowingPotion.COLOR_KEY, this.color);
    }

    public DyeColor color() {
        return this.color;
    }

    public void reset() {
        this.color = null;
        AffinityComponents.GLOWING_COLOR.sync(provider);
    }

    public void setColor(DyeColor color) {
        this.color = color;
        AffinityComponents.GLOWING_COLOR.sync(provider);
    }
}
