package com.glisco.nidween.util.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class GlowingColorComponent implements Component, AutoSyncedComponent {

    private final PlayerEntity provider;
    private String color = "green";

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
        NidweenComponents.GLOWING_COLOR.sync(provider);
    }
}
