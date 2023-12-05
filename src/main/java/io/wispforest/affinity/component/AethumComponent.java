package io.wispforest.affinity.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public abstract class AethumComponent<H> implements Component, AutoSyncedComponent {

    public static final KeyedEndec<Double> AETHUM_KEY = Endec.DOUBLE.keyed("Aethum", 0d);

    protected final ComponentKey<?> key;

    protected H holder;
    protected double aethum;

    public AethumComponent(ComponentKey<? extends AethumComponent<H>> key, H holder) {
        this.key = key;
        this.holder = holder;
        this.aethum = this.initialValue();
    }

    protected abstract double initialValue();

    protected abstract double maxAethum();

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        this.aethum = tag.get(AETHUM_KEY);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        tag.put(AETHUM_KEY, this.aethum);
    }

    public double addAethum(double value) {
        this.setAethum(this.aethum + value);
        return this.aethum;
    }

    public boolean tryConsumeAethum(double amount) {
        if (this.aethum < amount) return false;

        this.addAethum(-amount);
        return true;
    }

    public double getAethum() {
        return this.aethum;
    }

    public void setAethum(double aethum) {
        this.aethum = MathHelper.clamp(aethum, 0, this.maxAethum());
        this.key.sync(this.holder);
    }

}
