package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.*;
import io.wispforest.affinity.block.shadowed.*;
import io.wispforest.affinity.blockentity.impl.*;
import io.wispforest.affinity.mixin.access.SignTypeInvoker;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import io.wispforest.owo.registration.reflect.BlockRegistryContainer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.SignType;
import net.minecraft.util.registry.Registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

@SuppressWarnings("unused")
public class AffinityBlocks implements BlockRegistryContainer {

    @Tab(0) public static final Block BREWING_CAULDRON = new BrewingCauldronBlock();
    @Tab(0) public static final Block COPPER_PLATED_AETHUM_FLUX_NODE = new CopperPlatedAethumFluxNodeBlock();
    @Tab(0) public static final Block AETHUM_FLUX_CACHE = new AethumFluxCacheBlock();
    @Tab(0) public static final Block STONE_BANDED_AETHUM_FLUX_NODE = new StoneBandedAethumFluxNodeBlock();
    @Tab(0) public static final Block SUNDIAL = new SundialBlock();
    @Tab(0) public static final Block ARBOREAL_ACCUMULATION_APPARATUS = new ArborealAccumulationApparatusBlock();
    @Tab(0) public static final Block RUDIMENTARY_RITUAL_SOCLE = new RudimentaryRitualSocleBlock();
    @Tab(0) public static final Block ASP_RITE_CORE = new AspRiteCoreBlock();
    @Tab(0) public static final Block RITUAL_SOCLE_COMPOSER = new RitualSocleComposerBlock();

    public static final Block PECULIAR_CLUMP = new PeculiarClumpBlock();

    public static final Block AZALEA_PLANKS = new Block(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS));
    public static final Block AZALEA_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG));
    public static final Block STRIPPED_AZALEA_LOG = new PillarBlock(FabricBlockSettings.copyOf(Blocks.STRIPPED_OAK_LOG));
    public static final Block AZALEA_WOOD = new PillarBlock(FabricBlockSettings.copyOf(Blocks.OAK_WOOD));
    public static final Block STRIPPED_AZALEA_WOOD = new PillarBlock(FabricBlockSettings.copyOf(Blocks.STRIPPED_OAK_WOOD));
    public static final Block AZALEA_SLAB = new SlabBlock(FabricBlockSettings.copyOf(Blocks.OAK_SLAB));
    public static final Block AZALEA_FENCE = new FenceBlock(FabricBlockSettings.copyOf(Blocks.OAK_FENCE));
    public static final Block AZALEA_STAIRS = new AffinityStairsBlock(AZALEA_PLANKS.getDefaultState(), FabricBlockSettings.copyOf(Blocks.OAK_STAIRS));
    public static final Block AZALEA_BUTTON = new AffinityWoodenButtonBlock(FabricBlockSettings.copyOf(Blocks.OAK_BUTTON));
    public static final Block AZALEA_PRESSURE_PLATE = new AffinityPressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, FabricBlockSettings.copyOf(Blocks.OAK_PRESSURE_PLATE));
    public static final Block AZALEA_DOOR = new AffinityDoorBlock(FabricBlockSettings.copyOf(Blocks.OAK_DOOR));
    public static final Block AZALEA_TRAPDOOR = new AffinityTrapdoorBlock(FabricBlockSettings.copyOf(Blocks.OAK_TRAPDOOR));
    public static final Block AZALEA_FENCE_GATE = new FenceGateBlock(FabricBlockSettings.copyOf(Blocks.OAK_FENCE_GATE));

    public static final SignType AZALEA_SIGN_TYPE = SignTypeInvoker.affinity$invokeNew("azalea");
    @NoBlockItem public static final Block AZALEA_SIGN = new SignBlock(FabricBlockSettings.copyOf(Blocks.OAK_SIGN), AZALEA_SIGN_TYPE);
    @NoBlockItem public static final Block AZALEA_WALL_SIGN = new WallSignBlock(FabricBlockSettings.copyOf(Blocks.OAK_SIGN), AZALEA_SIGN_TYPE);

    public static BoatEntity.Type AZALEA_BOAT_TYPE;

    public static final Block BUDDING_AZALEA_LEAVES = new BuddingAzaleaLeavesBlock();
    public static final Block UNFLOWERING_AZALEA_LEAVES = new UnfloweringAzaleaLeavesBlock();

    @Override
    public void afterFieldProcessing() {
        StrippableBlockRegistry.register(AZALEA_LOG, STRIPPED_AZALEA_LOG);
        StrippableBlockRegistry.register(AZALEA_WOOD, STRIPPED_AZALEA_WOOD);
    }

    public static class Entities implements AutoRegistryContainer<BlockEntityType<?>> {

        public static final BlockEntityType<BrewingCauldronBlockEntity> BREWING_CAULDRON = FabricBlockEntityTypeBuilder.create(BrewingCauldronBlockEntity::new,
                AffinityBlocks.BREWING_CAULDRON).build();

        public static final BlockEntityType<AethumFluxNodeBlockEntity> AETHUM_FLUX_NODE = FabricBlockEntityTypeBuilder.create(AethumFluxNodeBlockEntity::new,
                AffinityBlocks.COPPER_PLATED_AETHUM_FLUX_NODE, AffinityBlocks.STONE_BANDED_AETHUM_FLUX_NODE).build();
        public static final BlockEntityType<AethumFluxCacheBlockEntity> AETHUM_FLUX_CACHE = FabricBlockEntityTypeBuilder.create(AethumFluxCacheBlockEntity::new,
                AffinityBlocks.AETHUM_FLUX_CACHE).build();

        public static final BlockEntityType<RitualSocleBlockEntity> RITUAL_SOCLE = FabricBlockEntityTypeBuilder.create(RitualSocleBlockEntity::new,
                AffinityBlocks.RUDIMENTARY_RITUAL_SOCLE).build();
        public static final BlockEntityType<RitualCoreBlockEntity> RITUAL_CORE = FabricBlockEntityTypeBuilder.create(RitualCoreBlockEntity::new,
                AffinityBlocks.ASP_RITE_CORE).build();

        public static final BlockEntityType<SundialBlockEntity> SUNDIAL = FabricBlockEntityTypeBuilder.create(SundialBlockEntity::new,
                AffinityBlocks.SUNDIAL).build();

        public static final BlockEntityType<ArborealAccumulationApparatusBlockEntity> ARBOREAL_ACCUMULATION_APPARATUS =
                FabricBlockEntityTypeBuilder.create(ArborealAccumulationApparatusBlockEntity::new, AffinityBlocks.ARBOREAL_ACCUMULATION_APPARATUS).build();

        @Override
        public void afterFieldProcessing() {
            Affinity.AETHUM_MEMBER.registerSelf(AETHUM_FLUX_NODE);
            Affinity.AETHUM_NODE.registerSelf(AETHUM_FLUX_NODE);

            Affinity.AETHUM_MEMBER.registerSelf(AETHUM_FLUX_CACHE);
            Affinity.AETHUM_MEMBER.registerSelf(BREWING_CAULDRON);
            Affinity.AETHUM_MEMBER.registerSelf(SUNDIAL);
            Affinity.AETHUM_MEMBER.registerSelf(ARBOREAL_ACCUMULATION_APPARATUS);
            Affinity.AETHUM_MEMBER.registerSelf(RITUAL_CORE);
        }

        @Override
        public Registry<BlockEntityType<?>> getRegistry() {
            return Registry.BLOCK_ENTITY_TYPE;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<BlockEntityType<?>> getTargetFieldType() {
            return (Class<BlockEntityType<?>>) (Object) BlockEntityType.class;
        }
    }

    @Override
    public void postProcessField(String namespace, Block value, String identifier, Field field) {
        if (field.isAnnotationPresent(NoBlockItem.class)) return;

        int tab = 1;
        if (field.isAnnotationPresent(Tab.class)) tab = field.getAnnotation(Tab.class).value();

        Registry.register(Registry.ITEM, new Identifier(namespace, identifier),
                new BlockItem(value, new OwoItemSettings().tab(tab).group(Affinity.AFFINITY_GROUP)));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    private @interface Tab {
        int value();
    }
}
