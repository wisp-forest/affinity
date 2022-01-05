package io.wispforest.affinity.worldgen;

import com.mojang.serialization.Codec;
import io.wispforest.affinity.Affinity;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.PostPlacementProcessor;
import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.structure.StructurePiecesGenerator;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

import java.util.Optional;

public class BikeshedFeature extends StructureFeature<StructurePoolFeatureConfig> {

    public BikeshedFeature(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, BikeshedFeature::createPiecesGenerator, PostPlacementProcessor.EMPTY);
    }

    private static boolean isFeatureChunk(StructureGeneratorFactory.Context<StructurePoolFeatureConfig> context) {
        final var spawnXZ = context.chunkPos().getCenterAtY(0);
        final int landHeight = context.chunkGenerator().getHeightInGround(spawnXZ.getX(), spawnXZ.getZ(), Heightmap.Type.WORLD_SURFACE_WG, context.world());

        var columnSample = context.chunkGenerator().getColumnSample(spawnXZ.getX(), spawnXZ.getZ(), context.world());
        var topState = columnSample.getState(landHeight);

        return topState.getFluidState().isEmpty();
    }

    public static Optional<StructurePiecesGenerator<StructurePoolFeatureConfig>> createPiecesGenerator(StructureGeneratorFactory.Context<StructurePoolFeatureConfig> context) {

        if (!isFeatureChunk(context)) return Optional.empty();

        var config = new StructurePoolFeatureConfig(
                () -> context.registryManager().get(Registry.STRUCTURE_POOL_KEY).get(Affinity.id("bikeshed/pool")),
                1);

        var newContext = new StructureGeneratorFactory.Context<>(
                context.chunkGenerator(),
                context.biomeSource(),
                context.seed(),
                context.chunkPos(),
                config,
                context.world(),
                context.validBiome(),
                context.structureManager(),
                context.registryManager()
        );

        var centerPos = context.chunkPos().getCenterAtY(0);

        var pieceGenerator = StructurePoolBasedGenerator.generate(
                newContext,
                PoolStructurePiece::new,
                centerPos,
                false,
                true
        );

        if (pieceGenerator.isPresent()) Affinity.LOGGER.info("Bikeshed at {}", centerPos);

        return pieceGenerator;
    }

}
