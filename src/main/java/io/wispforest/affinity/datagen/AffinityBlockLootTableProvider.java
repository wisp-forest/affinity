package io.wispforest.affinity.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;

import static io.wispforest.affinity.object.AffinityBlocks.*;

public class AffinityBlockLootTableProvider extends FabricBlockLootTableProvider {

    public AffinityBlockLootTableProvider(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generate() {
        this.selfDrop(
                BREWING_CAULDRON, COPPER_PLATED_AETHUM_FLUX_NODE, AETHUM_FLUX_CACHE, CREATIVE_AETHUM_FLUX_CACHE, STONE_BANDED_AETHUM_FLUX_NODE, SUNDIAL,
                ARBOREAL_ACCUMULATION_APPARATUS, BLANK_RITUAL_SOCLE, RUDIMENTARY_RITUAL_SOCLE, REFINED_RITUAL_SOCLE, SOPHISTICATED_RITUAL_SOCLE, ASSEMBLY_AUGMENT,
                SPIRIT_INTEGRATION_APPARATUS, RITUAL_SOCLE_COMPOSER, AFFINE_INFUSER, RANTHRACITE_WIRE, CROP_REAPER, WORLD_PIN, SUNDIAL, ARCANE_TREETAP, ASSEMBLY_AUGMENT,
                STAFF_PEDESTAL, OUIJA_BOARD, ITEM_TRANSFER_NODE, AETHUM_PROBE, EMERALD_BLOCK, AFFINE_CANDLE, EMERALD_BLOCK, THE_SKY, INVERSION_STONE, INFUSED_STONE
        );

        this.addDrop(BUDDING_AZALEA_LEAVES, block -> dropsWithShears(BUDDING_AZALEA_LEAVES));
        this.addDrop(UNFLOWERING_AZALEA_LEAVES, block -> dropsWithShears(UNFLOWERING_AZALEA_LEAVES));

        this.selfDrop(AZALEA_LOG, AZALEA_WOOD, STRIPPED_AZALEA_LOG, STRIPPED_AZALEA_WOOD);
        for (var block : AffinityBlockFamilies.AZALEA.getVariants().values()) {
            this.drops(block);
        }
    }

    private void selfDrop(Block... blocks) {
        for (var block : blocks) this.addDrop(block);
    }
}
