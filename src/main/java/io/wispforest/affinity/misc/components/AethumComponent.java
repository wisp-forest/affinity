package io.wispforest.affinity.misc.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;

public abstract class AethumComponent<H> implements Component, AutoSyncedComponent {

    public static final String AETHUM_KEY = "Aethum";

    private final ComponentKey<?> key;

    protected H holder;
    protected double aethum;

    public AethumComponent(ComponentKey<? extends AethumComponent<H>> key, H holder) {
        this.key = key;
        this.holder = holder;
        this.aethum = this.defaultValue();
    }

    abstract double defaultValue();

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.aethum = tag.getDouble(AETHUM_KEY);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putDouble(AETHUM_KEY, this.aethum);
    }

    public double getAethum() {
        return this.aethum;
    }

    public void setAethum(double aethum) {
        this.aethum = aethum;
        this.key.sync(this.holder);
    }

}
