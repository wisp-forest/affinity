package io.wispforest.affinity.component;


import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

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
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        this.aethum = tag.get(AETHUM_KEY);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
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
