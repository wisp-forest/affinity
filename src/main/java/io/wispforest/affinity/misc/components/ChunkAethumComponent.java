package io.wispforest.affinity.misc.components;

import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import java.util.concurrent.ThreadLocalRandom;

public class ChunkAethumComponent extends AethumComponent<Chunk> implements ServerTickingComponent {

    public ChunkAethumComponent(Chunk chunk) {
        super(AffinityComponents.CHUNK_AETHUM, chunk);
    }

    public double adjustedAethum() {
        if (!(this.holder instanceof WorldChunk worldChunk)) return -1;
        var world = worldChunk.getWorld();

        double mean = this.aethum;
        double min = this.aethum;
        double max = this.aethum;
        var pos = this.holder.getPos();

        for (var dir : Direction.values()) {
            if (dir.getAxis().isVertical()) continue;
            final var aethum = AffinityComponents.CHUNK_AETHUM.get(world.getChunk(pos.x + dir.getOffsetX(), pos.z + dir.getOffsetZ())).getAethum();
            mean += aethum;

            if (aethum < min) min = aethum;
            if (aethum > max) max = aethum;
        }

        mean /= 5;

        if (Math.abs(mean - min) > Math.abs(mean - max)) {
            mean += min * 4;
        } else {
            mean += max * 4;
        }

        return mean / 5;
    }

    public double aethumAt(BlockPos pos) {
        pos = new BlockPos(pos.getX(), 0, pos.getZ());

        if (!(this.holder instanceof WorldChunk worldChunk)) return -1;
        var world = worldChunk.getWorld();

        final var chunkPos = worldChunk.getPos();
        final var chunks = new Chunk[25];

        int idx = 0;
        for (int x = chunkPos.x - 2; x <= chunkPos.x + 2; x++) {
            for (int z = chunkPos.z - 2; z <= chunkPos.z + 2; z++) {
                chunks[idx++] = world.getChunk(x, z);
            }
        }

        double numerator = 0;
        double denominator = 0;

        for (var chunk : chunks) {
            final var distance = Math.pow(Math.sqrt(pos.getSquaredDistance(getCenter(chunk))), -3.5);
            numerator += distance * AffinityComponents.CHUNK_AETHUM.get(chunk).adjustedAethum();
            denominator += distance;
        }

        return numerator / denominator;
    }

    private static BlockPos getCenter(Chunk chunk) {
        final var pos = chunk.getPos();
        return new BlockPos(pos.x * 16 + 8, 0, pos.z * 16 + 8);
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
