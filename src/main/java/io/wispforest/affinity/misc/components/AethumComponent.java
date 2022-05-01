package io.wispforest.affinity.misc.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.wispforest.owo.util.NbtKey;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

public abstract class AethumComponent<H> implements Component, AutoSyncedComponent {

    public static final NbtKey<Double> AETHUM_KEY = new NbtKey<>("Aethum", NbtKey.Type.DOUBLE);

    protected final ComponentKey<?> key;

    protected H holder;
    protected double aethum;

    public AethumComponent(ComponentKey<? extends AethumComponent<H>> key, H holder) {
        this.key = key;
        this.holder = holder;
        this.aethum = this.initialValue();
    }

    abstract double initialValue();

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        this.aethum = AETHUM_KEY.getOr(tag, this.aethum);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        AETHUM_KEY.put(tag, this.aethum);
    }

    public double addAethum(double value) {
        this.setAethum(this.aethum + value);
        return this.aethum;
    }

    public double getAethum() {
        return this.aethum;
    }

    public void setAethum(double aethum) {
        this.aethum = aethum;
        this.key.sync(this.holder);
    }

}
