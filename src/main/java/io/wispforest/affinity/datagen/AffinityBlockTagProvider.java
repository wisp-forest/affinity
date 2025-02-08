package io.wispforest.affinity.datagen;

import io.wispforest.affinity.Affinity;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

import static io.wispforest.affinity.object.AffinityBlocks.*;

public class AffinityBlockTagProvider extends FabricTagProvider.BlockTagProvider {

    public static final TagKey<Block> AZALEA_LOGS = TagKey.of(RegistryKeys.BLOCK, Affinity.id("azalea_logs"));

    public AffinityBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {

        // General mineability stuff

        this.getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(
                BREWING_CAULDRON, COPPER_PLATED_AETHUM_FLUX_NODE, AETHUM_FLUX_CACHE, CREATIVE_AETHUM_FLUX_CACHE, SUNDIAL, BLANK_RITUAL_SOCLE,
                RUDIMENTARY_RITUAL_SOCLE, REFINED_RITUAL_SOCLE, SOPHISTICATED_RITUAL_SOCLE, SPIRIT_INTEGRATION_APPARATUS, ARCANE_TREETAP, STAFF_PEDESTAL,
                OUIJA_BOARD, ITEM_TRANSFER_NODE, EMERALD_BLOCK, PECULIAR_CLUMP, THE_SKY, INVERSION_STONE, SUNSHINE_MONOLITH, INFUSED_STONE,
                RITUAL_SOCLE_COMPOSER, AFFINE_INFUSER, FIELD_COHERENCE_MODULATOR, HOLOGRAPHIC_STEREOPTICON, GRAVITON_TRANSDUCER, ETHEREAL_AETHUM_FLUX_NODE,
                ETHEREAL_AETHUM_FLUX_INJECTOR, WORLD_PIN, LOCAL_DISPLACEMENT_GATEWAY
        );

        this.getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL).add(EMERALD_BLOCK, PECULIAR_CLUMP, INFUSED_STONE);

        this.getOrCreateTagBuilder(BlockTags.AXE_MINEABLE).add(
                STONE_BANDED_AETHUM_FLUX_NODE, ARBOREAL_ANNIHILATION_APPARATUS, ASP_RITE_CORE, ASSEMBLY_AUGMENT,
                MATTER_HARVESTING_HEARTH, CROP_REAPER, AZALEA_CHEST, VILLAGER_ARMATURE
        );

        this.getOrCreateTagBuilder(BlockTags.HOE_MINEABLE).add(
                BUDDING_AZALEA_LEAVES, UNFLOWERING_AZALEA_LEAVES
        );

        // Azalea wood set

        this.getOrCreateTagBuilder(BlockTags.AXE_MINEABLE).add(AffinityBlockFamilies.AZALEA.getVariants().values().stream()
                .sorted(Comparator.comparing(block -> block.getRegistryEntry().registryKey().getValue()))
                .toArray(Block[]::new)
        );

        this.getOrCreateTagBuilder(AZALEA_LOGS).add(AZALEA_LOG, AZALEA_WOOD, STRIPPED_AZALEA_LOG, STRIPPED_AZALEA_WOOD);
        this.getOrCreateTagBuilder(BlockTags.LOGS_THAT_BURN).addTag(AZALEA_LOGS);
        this.getOrCreateTagBuilder(BlockTags.OVERWORLD_NATURAL_LOGS).add(AZALEA_LOG);
        this.getOrCreateTagBuilder(BlockTags.LEAVES).add(BUDDING_AZALEA_LEAVES, UNFLOWERING_AZALEA_LEAVES);

        this.getOrCreateTagBuilder(BlockTags.PLANKS).add(AZALEA_PLANKS);
        this.getOrCreateTagBuilder(BlockTags.STANDING_SIGNS).add(AZALEA_SIGN);
        this.getOrCreateTagBuilder(BlockTags.WALL_SIGNS).add(AZALEA_WALL_SIGN);
        this.getOrCreateTagBuilder(BlockTags.CEILING_HANGING_SIGNS).add(AZALEA_HANGING_SIGN);
        this.getOrCreateTagBuilder(BlockTags.WALL_HANGING_SIGNS).add(AZALEA_WALL_HANGING_SIGN);
        this.getOrCreateTagBuilder(BlockTags.WOODEN_BUTTONS).add(AZALEA_BUTTON);
        this.getOrCreateTagBuilder(BlockTags.WOODEN_DOORS).add(AZALEA_DOOR);
        this.getOrCreateTagBuilder(BlockTags.WOODEN_FENCES).add(AZALEA_FENCE);
        this.getOrCreateTagBuilder(BlockTags.WOODEN_SLABS).add(AZALEA_SLAB);
        this.getOrCreateTagBuilder(BlockTags.WOODEN_STAIRS).add(AZALEA_STAIRS);
        this.getOrCreateTagBuilder(BlockTags.WOODEN_PRESSURE_PLATES).add(AZALEA_PRESSURE_PLATE);
        this.getOrCreateTagBuilder(BlockTags.WOODEN_TRAPDOORS).add(AZALEA_TRAPDOOR);

        // Misc

        this.getOrCreateTagBuilder(BlockTags.CANDLES).add(AFFINE_CANDLE);
        this.getOrCreateTagBuilder(BlockTags.BEACON_BASE_BLOCKS).add(EMERALD_BLOCK);

        this.getOrCreateTagBuilder(ConventionalBlockTags.ORES).add(PECULIAR_CLUMP);

        this.getOrCreateTagBuilder(ConventionalBlockTags.CHESTS).add(AZALEA_CHEST);
        this.getOrCreateTagBuilder(BlockTags.GUARDED_BY_PIGLINS).add(AZALEA_CHEST);
        this.getOrCreateTagBuilder(BlockTags.FEATURES_CANNOT_REPLACE).add(AZALEA_CHEST);
    }
}
