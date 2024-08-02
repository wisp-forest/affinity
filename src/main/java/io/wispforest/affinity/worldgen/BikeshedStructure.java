package io.wispforest.affinity.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.Affinity;
import io.wispforest.owo.util.RegistryAccess;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructureLiquidSettings;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.pool.alias.StructurePoolAliasLookup;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.DimensionPadding;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

// TODO possibly replace with json
public class BikeshedStructure extends Structure {

    public static final MapCodec<BikeshedStructure> CODEC = createCodec(BikeshedStructure::new);

    protected BikeshedStructure(Config config) {
        super(config);
    }

    private static boolean isFeatureChunk(Context context) {
        final var spawnXZ = context.chunkPos().getCenterAtY(0);
        final int landHeight = context.chunkGenerator().getHeightInGround(spawnXZ.getX(), spawnXZ.getZ(), Heightmap.Type.WORLD_SURFACE_WG, context.world(), context.noiseConfig());

        var columnSample = context.chunkGenerator().getColumnSample(spawnXZ.getX(), spawnXZ.getZ(), context.world(), context.noiseConfig());
        var topState = columnSample.getState(landHeight);

        return topState.getFluidState().isEmpty();
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Context context) {
        if (!isFeatureChunk(context)) return Optional.empty();

        var centerPos = context.chunkPos().getCenterAtY(0);
        var poolRegistry = context.dynamicRegistryManager().get(RegistryKeys.TEMPLATE_POOL);

        return StructurePoolBasedGenerator.generate(
                context,
                RegistryAccess.getEntry(poolRegistry, Affinity.id("bikeshed/pool")),
                Optional.empty(),
                1,
                centerPos,
                false,
                Optional.of(Heightmap.Type.WORLD_SURFACE_WG),
                16,
                StructurePoolAliasLookup.EMPTY,
                DimensionPadding.NONE,
                StructureLiquidSettings.IGNORE_WATERLOGGING
        );
    }

    @Override
    public StructureType<?> getType() {
        return AffinityStructures.BIKESHED;
    }
}
