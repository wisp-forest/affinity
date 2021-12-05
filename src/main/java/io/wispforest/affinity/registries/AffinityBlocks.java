package io.wispforest.affinity.registries;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.*;
import io.wispforest.affinity.blockentity.*;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import io.wispforest.owo.registration.reflect.BlockRegistryContainer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class AffinityBlocks implements BlockRegistryContainer {

    public static final Block BREWING_CAULDRON = new BrewingCauldronBlock();
    public static final Block COPPER_PLATED_AETHER_FLUX_NODE = new CopperPlatedAetherFluxNodeBlock();
    public static final Block COPPER_PLATED_AETHER_FLUX_CACHE = new CopperPlatedAetherFluxCacheBlock();
    public static final Block STONE_BANDED_AETHER_FLUX_NODE = new StoneBandedAetherFluxNodeBlock();
    public static final Block SUNDIAL = new SundialBlock();

    @Override
    public BlockItem createBlockItem(Block block, String identifier) {
        return new BlockItem(block, new Item.Settings().group(Affinity.AFFINITY_GROUP));
    }

    public static class Entities implements AutoRegistryContainer<BlockEntityType<?>> {

        public static final BlockEntityType<BrewingCauldronBlockEntity> BREWING_CAULDRON = FabricBlockEntityTypeBuilder.create(BrewingCauldronBlockEntity::new,
                AffinityBlocks.BREWING_CAULDRON).build();

        public static final BlockEntityType<AetherFluxNodeBlockEntity> AETHER_FLUX_NODE = FabricBlockEntityTypeBuilder.create(AetherFluxNodeBlockEntity::new,
                AffinityBlocks.COPPER_PLATED_AETHER_FLUX_NODE, AffinityBlocks.STONE_BANDED_AETHER_FLUX_NODE).build();
        public static final BlockEntityType<AetherFluxCacheBlockEntity> AETHER_FLUX_CACHE = FabricBlockEntityTypeBuilder.create(AetherFluxCacheBlockEntity::new,
                AffinityBlocks.COPPER_PLATED_AETHER_FLUX_CACHE).build();

        public static final BlockEntityType<SundialBlockEntity> SUNDIAL = FabricBlockEntityTypeBuilder.create(SundialBlockEntity::new,
                AffinityBlocks.SUNDIAL).build();

        @Override
        public Registry<BlockEntityType<?>> getRegistry() {
            return Registry.BLOCK_ENTITY_TYPE;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<BlockEntityType<?>> getTargetFieldType() {
            return (Class<BlockEntityType<?>>) (Object) BlockEntityType.class;
        }

        @Override
        public void afterFieldProcessing() {
            Affinity.AETHER_MEMBER.registerSelf(AETHER_FLUX_NODE);
            Affinity.AETHER_NODE.registerSelf(AETHER_FLUX_NODE);

            Affinity.AETHER_MEMBER.registerSelf(AETHER_FLUX_CACHE);
            Affinity.AETHER_MEMBER.registerSelf(BREWING_CAULDRON);
            Affinity.AETHER_MEMBER.registerSelf(SUNDIAL);
        }
    }

}
