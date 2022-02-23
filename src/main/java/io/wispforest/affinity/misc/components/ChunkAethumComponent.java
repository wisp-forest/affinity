package io.wispforest.affinity.misc.components;

import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.world.chunk.Chunk;

import java.util.concurrent.ThreadLocalRandom;

public class ChunkAethumComponent extends AethumComponent<Chunk> implements ServerTickingComponent {

    public ChunkAethumComponent(Chunk chunk) {
        super(AffinityComponents.CHUNK_AETHUM, chunk);
    }

    @Override
    public void setAethum(double aethum) {
        super.setAethum(aethum);
        this.holder.setShouldSave(true);
    }

    @Override
    public void serverTick() {
        if (this.aethum != this.defaultValue()) return;
        this.regenerate();
    }

    public void regenerate() {
        this.aethum = ThreadLocalRandom.current().nextDouble(50, 75);
        this.holder.setShouldSave(true);
    }

    @Override
    double defaultValue() {
        return -1;
    }
}
