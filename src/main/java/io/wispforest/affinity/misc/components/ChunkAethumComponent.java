package io.wispforest.affinity.misc.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.chunk.Chunk;

import java.util.concurrent.ThreadLocalRandom;

public class ChunkAethumComponent implements Component, ServerTickingComponent {

    private static final String AETHUM_KEY = "Aethum";

    private final Chunk holder;
    private double aethum = -1;

    public ChunkAethumComponent(Chunk chunk) {
        this.holder = chunk;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.aethum = tag.getDouble(AETHUM_KEY);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putDouble(AETHUM_KEY, this.aethum);
    }

    public double getAethum() {
        return aethum;
    }

    public void setAethum(double aethum) {
        this.aethum = aethum;
        this.holder.setShouldSave(true);
    }

    @Override
    public void serverTick() {
        if (this.aethum != -1) return;
        this.regenerate();
    }

    public void regenerate() {
        this.aethum = ThreadLocalRandom.current().nextDouble(50, 75);
        this.holder.setShouldSave(true);
    }
}
