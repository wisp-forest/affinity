package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.MangroveBasketBlock;
import io.wispforest.affinity.block.impl.*;
import io.wispforest.affinity.block.shadowed.*;
import io.wispforest.affinity.block.template.BlockItemProvider;
import io.wispforest.affinity.blockentity.impl.*;
import io.wispforest.affinity.item.AffinityItemGroup;
import io.wispforest.affinity.mixin.access.SignTypeInvoker;
import io.wispforest.affinity.object.rituals.AffinityRitualSocleTypes;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import io.wispforest.owo.registration.reflect.BlockRegistryContainer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.SignType;
import net.minecraft.util.registry.Registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public class AffinityBlocks implements BlockRegistryContainer {

    @Tab(AffinityItemGroup.MAIN) public static final Block BREWING_CAULDRON = new BrewingCauldronBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block COPPER_PLATED_AETHUM_FLUX_NODE = new CopperPlatedAethumFluxNodeBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block AETHUM_FLUX_CACHE = new AethumFluxCacheBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block STONE_BANDED_AETHUM_FLUX_NODE = new StoneBandedAethumFluxNodeBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block SUNDIAL = new SundialBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block ARBOREAL_ACCUMULATION_APPARATUS = new ArborealAccumulationApparatusBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block BLANK_RITUAL_SOCLE = new BlankRitualSocleBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block RUDIMENTARY_RITUAL_SOCLE = new RitualSocleBlock(AffinityRitualSocleTypes.RUDIMENTARY);
    @Tab(AffinityItemGroup.MAIN) public static final Block REFINED_RITUAL_SOCLE = new RitualSocleBlock(AffinityRitualSocleTypes.REFINED);
    @Tab(AffinityItemGroup.MAIN) public static final Block SOPHISTICATED_RITUAL_SOCLE = new RitualSocleBlock(AffinityRitualSocleTypes.SOPHISTICATED);
    @Tab(AffinityItemGroup.MAIN) public static final Block ASP_RITE_CORE = new AspRiteCoreBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block ABERRANT_CALLING_CORE = new AberrantCallingCoreBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block RITUAL_SOCLE_COMPOSER = new RitualSocleComposerBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block AFFINE_INFUSER = new AffineInfuserBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block RANTHRACITE_WIRE = new RanthraciteWireBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block CROP_REAPER = new CropReaperBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block WORLD_PIN = new WorldPinBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block SUNSHINE_MONOLITH = new SunshineMonolithBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block ARCANE_TREETAP = new ArcaneTreetapBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block ASSEMBLY_AUGMENT = new AssemblyAugmentBlock();
    @Tab(AffinityItemGroup.MAIN) public static final Block MANGROVE_BASKET = new MangroveBasketBlock();

    @Tab(AffinityItemGroup.MAIN) public static final AffineCandleBlock AFFINE_CANDLE = new AffineCandleBlock();

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

        public static final BlockEntityType<BrewingCauldronBlockEntity> BREWING_CAULDRON =
                make(BrewingCauldronBlockEntity::new, AffinityBlocks.BREWING_CAULDRON);

        public static final BlockEntityType<AethumFluxNodeBlockEntity> AETHUM_FLUX_NODE =
                make(AethumFluxNodeBlockEntity::new,
                        AffinityBlocks.COPPER_PLATED_AETHUM_FLUX_NODE,
                        AffinityBlocks.STONE_BANDED_AETHUM_FLUX_NODE);

        public static final BlockEntityType<AethumFluxCacheBlockEntity> AETHUM_FLUX_CACHE =
                make(AethumFluxCacheBlockEntity::new, AffinityBlocks.AETHUM_FLUX_CACHE);

        public static final BlockEntityType<RitualSocleBlockEntity> RITUAL_SOCLE =
                make(RitualSocleBlockEntity::new,
                        AffinityBlocks.RUDIMENTARY_RITUAL_SOCLE,
                        AffinityBlocks.REFINED_RITUAL_SOCLE,
                        AffinityBlocks.SOPHISTICATED_RITUAL_SOCLE);

        public static final BlockEntityType<AspRiteCoreBlockEntity> ASP_RITE_CORE =
                make(AspRiteCoreBlockEntity::new, AffinityBlocks.ASP_RITE_CORE);
        public static final BlockEntityType<AberrantCallingCoreBlockEntity> ABERRANT_CALLING_CORE =
                make(AberrantCallingCoreBlockEntity::new, AffinityBlocks.ABERRANT_CALLING_CORE);

        public static final BlockEntityType<AffineInfuserBlockEntity> AFFINE_INFUSER =
                make(AffineInfuserBlockEntity::new, AffinityBlocks.AFFINE_INFUSER);

        public static final BlockEntityType<SundialBlockEntity> SUNDIAL =
                make(SundialBlockEntity::new, AffinityBlocks.SUNDIAL);

        public static final BlockEntityType<ArborealAccumulationApparatusBlockEntity> ARBOREAL_ACCUMULATION_APPARATUS =
                make(ArborealAccumulationApparatusBlockEntity::new, AffinityBlocks.ARBOREAL_ACCUMULATION_APPARATUS);

        public static final BlockEntityType<CropReaperBlockEntity> CROP_REAPER =
                make(CropReaperBlockEntity::new, AffinityBlocks.CROP_REAPER);

        public static final BlockEntityType<AffineCandleBlockEntity> AFFINE_CANDLE =
                make(AffineCandleBlockEntity::new, AffinityBlocks.AFFINE_CANDLE);

        public static final BlockEntityType<WorldPinBlockEntity> WORLD_PIN =
                make(WorldPinBlockEntity::new, AffinityBlocks.WORLD_PIN);

        public static final BlockEntityType<SunshineMonolithBlockEntity> SUNSHINE_MONOLITH =
                make(SunshineMonolithBlockEntity::new, AffinityBlocks.SUNSHINE_MONOLITH);

        public static final BlockEntityType<AssemblyAugmentBlockEntity> ASSEMBLY_AUGMENT =
                make(AssemblyAugmentBlockEntity::new, AffinityBlocks.ASSEMBLY_AUGMENT);

        public static final BlockEntityType<MangroveBasketBlockEntity> MANGROVE_BASKET =
                make(MangroveBasketBlockEntity::new, AffinityBlocks.MANGROVE_BASKET);

        @Override
        public void afterFieldProcessing() {
            Affinity.AETHUM_MEMBER.registerSelf(AETHUM_FLUX_NODE);
            Affinity.AETHUM_NODE.registerSelf(AETHUM_FLUX_NODE);

            Affinity.AETHUM_MEMBER.registerSelf(AETHUM_FLUX_CACHE);
            Affinity.AETHUM_MEMBER.registerSelf(BREWING_CAULDRON);
            Affinity.AETHUM_MEMBER.registerSelf(SUNDIAL);
            Affinity.AETHUM_MEMBER.registerSelf(ARBOREAL_ACCUMULATION_APPARATUS);
            Affinity.AETHUM_MEMBER.registerSelf(ASP_RITE_CORE);
            Affinity.AETHUM_MEMBER.registerSelf(ABERRANT_CALLING_CORE);
            Affinity.AETHUM_MEMBER.registerSelf(AFFINE_INFUSER);
            Affinity.AETHUM_MEMBER.registerSelf(CROP_REAPER);
            Affinity.AETHUM_MEMBER.registerSelf(AFFINE_CANDLE);
            Affinity.AETHUM_MEMBER.registerSelf(WORLD_PIN);
            Affinity.AETHUM_MEMBER.registerSelf(SUNSHINE_MONOLITH);
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

        private static <T extends BlockEntity> BlockEntityType<T> make(FabricBlockEntityTypeBuilder.Factory<T> factory, Block... blocks) {
            return FabricBlockEntityTypeBuilder.create(factory, blocks).build();
        }
    }

    @Override
    public void postProcessField(String namespace, Block value, String identifier, Field field) {
        if (field.isAnnotationPresent(NoBlockItem.class)) return;

        int tab = AffinityItemGroup.NATURE;
        if (field.isAnnotationPresent(Tab.class)) tab = field.getAnnotation(Tab.class).value();

        BiFunction<Block, OwoItemSettings, Item> factory = value instanceof BlockItemProvider provider
                ? provider::createBlockItem
                : BlockItem::new;

        Registry.register(Registry.ITEM, new Identifier(namespace, identifier),
                factory.apply(value, new OwoItemSettings().tab(tab).group(Affinity.AFFINITY_GROUP)));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    private @interface Tab {
        int value();
    }
}
