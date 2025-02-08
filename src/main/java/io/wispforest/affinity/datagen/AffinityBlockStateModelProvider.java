package io.wispforest.affinity.datagen;

import io.wispforest.affinity.object.AffinityItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.*;
import net.minecraft.item.ItemConvertible;

import static io.wispforest.affinity.object.AffinityBlocks.*;

public class AffinityBlockStateModelProvider extends FabricModelProvider {

    public AffinityBlockStateModelProvider(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {

        // Azalea wood set

        generator.registerCubeAllModelTexturePool(AZALEA_PLANKS).family(AffinityBlockFamilies.AZALEA);
        generator.registerLog(AZALEA_LOG).log(AZALEA_LOG).wood(AZALEA_WOOD);
        generator.registerLog(STRIPPED_AZALEA_LOG).log(STRIPPED_AZALEA_LOG).wood(STRIPPED_AZALEA_WOOD);
        generator.registerHangingSign(STRIPPED_AZALEA_LOG, AZALEA_HANGING_SIGN, AZALEA_WALL_HANGING_SIGN);

        // Everything else

        this.primitiveCubeAllState(generator,
                PECULIAR_CLUMP, UNFLOWERING_AZALEA_LEAVES, INVERSION_STONE, INFUSED_STONE
        );

        generator.excludeFromSimpleItemModelGeneration(AFFINE_INFUSER);
        generator.excludeFromSimpleItemModelGeneration(FIELD_COHERENCE_MODULATOR);
        generator.excludeFromSimpleItemModelGeneration(AZALEA_CHEST);
        generator.excludeFromSimpleItemModelGeneration(VILLAGER_ARMATURE);
        this.primitiveState(generator,
                STONE_BANDED_AETHUM_FLUX_NODE, ARBOREAL_ANNIHILATION_APPARATUS, BLANK_RITUAL_SOCLE, RUDIMENTARY_RITUAL_SOCLE,
                REFINED_RITUAL_SOCLE, SOPHISTICATED_RITUAL_SOCLE, SPIRIT_INTEGRATION_APPARATUS, ASP_RITE_CORE, ASSEMBLY_AUGMENT,
                CREATIVE_AETHUM_FLUX_CACHE, EMERALD_BLOCK, SUNDIAL, BREWING_CAULDRON, WORLD_PIN, CROP_REAPER, AFFINE_INFUSER, VOID_BEACON,
                FIELD_COHERENCE_MODULATOR, GRAVITON_TRANSDUCER, ETHEREAL_AETHUM_FLUX_NODE, LOCAL_DISPLACEMENT_GATEWAY, AZALEA_CHEST
        );

        generator.registerNorthDefaultHorizontalRotation(SONIC_SYPHON);
        generator.registerNorthDefaultHorizontalRotation(VILLAGER_ARMATURE);
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
        this.parentedItem(generator,
                ITEM_TRANSFER_NODE, PECULIAR_CLUMP, UNFLOWERING_AZALEA_LEAVES, THE_SKY, INVERSION_STONE, COPPER_PLATED_AETHUM_FLUX_NODE,
                STONE_BANDED_AETHUM_FLUX_NODE, ARBOREAL_ANNIHILATION_APPARATUS, BLANK_RITUAL_SOCLE, RUDIMENTARY_RITUAL_SOCLE, REFINED_RITUAL_SOCLE,
                SOPHISTICATED_RITUAL_SOCLE, RITUAL_SOCLE_COMPOSER, SPIRIT_INTEGRATION_APPARATUS, ASP_RITE_CORE, ASSEMBLY_AUGMENT, STAFF_PEDESTAL,
                CREATIVE_AETHUM_FLUX_CACHE, EMERALD_BLOCK, INFUSED_STONE, MATTER_HARVESTING_HEARTH, VOID_BEACON, HOLOGRAPHIC_STEREOPTICON
        );

        this.generatedItem(generator,
                SUNDIAL, BREWING_CAULDRON, AffinityItems.RANTHRACITE_DUST
        );
    }

    private void parentedItem(ItemModelGenerator generator, Block... blocks) {
        for (var block : blocks) {
            generator.writer.accept(ModelIds.getItemModelId(block.asItem()), new SimpleModelSupplier(ModelIds.getBlockModelId(block)));
        }
    }

    private void generatedItem(ItemModelGenerator generator, ItemConvertible... items) {
        for (var itemConvertible : items) generator.register(itemConvertible.asItem(), Models.GENERATED);
    }

    private void primitiveState(BlockStateModelGenerator generator, Block... blocks) {
        for (var block : blocks) generator.registerSimpleState(block);
    }

    private void primitiveCubeAllState(BlockStateModelGenerator generator, Block... blocks) {
        for (var block : blocks) generator.registerSimpleCubeAll(block);
    }

    @Override
    public String getName() {
        return "Block States / Models";
    }
}
