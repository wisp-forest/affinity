package io.wispforest.affinity.util;

import io.wispforest.affinity.misc.components.AffinityComponents;
import io.wispforest.affinity.misc.components.ChunkAethumComponent;
import net.minecraft.world.World;

public class AethumAcquisitionCache {

    private final ChunkAethumComponent[] chunkComponentCache;
    private final double[] adjustedChunkAethumCache;

    private final int sideLength;
    private final int topLeftX;
    private final int topLeftZ;

    public static AethumAcquisitionCache create(World world, int topLeftX, int topLeftZ, int sideLength) {
        return new AethumAcquisitionCache(world, topLeftX - 2, topLeftZ - 2, sideLength + 4);
    }

    public AethumAcquisitionCache(World world, int topLeftX, int topLeftZ, int sideLength) {

        final var chunkCount = sideLength * sideLength;

        this.chunkComponentCache = new ChunkAethumComponent[chunkCount];
        this.adjustedChunkAethumCache = new double[chunkCount];
        this.sideLength = sideLength;
        this.topLeftX = topLeftX;
        this.topLeftZ = topLeftZ;

        for (int i = 0; i < chunkCount; i++) {
            final var component = AffinityComponents.CHUNK_AETHUM.get(
                    world.getChunk(topLeftX + i / sideLength, topLeftZ + i % sideLength));

            this.chunkComponentCache[i] = component;
            this.adjustedChunkAethumCache[i] = component.adjustedAethum();
        }
    }

    public ChunkAethumComponent getComponentFrom(int x, int z) {
        return this.chunkComponentCache[(x - topLeftX) * this.sideLength + (z - topLeftZ)];
    }

    public double getAdjustedAethumFromCache(int x, int z) {
        return this.adjustedChunkAethumCache[(x - topLeftX) * this.sideLength + (z - topLeftZ)];
    }

}
