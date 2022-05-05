package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;

import java.util.Objects;

public class GlowingColorComponent implements Component, AutoSyncedComponent {

    private final PlayerEntity provider;
    private String color = "none";

    public GlowingColorComponent(PlayerEntity provider) {
        this.provider = provider;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.color = tag.getString("Color");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putString("Color", this.color);
    }

    public DyeColor getColor() {
        return Objects.equals(color, "none") ? null : DyeColor.byName(color, DyeColor.WHITE);
    }

    public void reset() {
        this.color = "none";
        AffinityComponents.GLOWING_COLOR.sync(provider);
    }

    public void setColor(String color) {
        this.color = color;
        AffinityComponents.GLOWING_COLOR.sync(provider);
    }
}
