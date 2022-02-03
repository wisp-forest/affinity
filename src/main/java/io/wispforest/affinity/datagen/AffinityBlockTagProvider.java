package io.wispforest.affinity.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.tag.BlockTags;

import static io.wispforest.affinity.object.AffinityBlocks.*;

public class AffinityBlockTagProvider extends FabricTagProvider.BlockTagProvider {

    public AffinityBlockTagProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateTags() {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(BREWING_CAULDRON, COPPER_PLATED_AETHUM_FLUX_NODE, AETHUM_FLUX_CACHE, SUNDIAL, PECULIAR_CLUMP);

        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(STONE_BANDED_AETHUM_FLUX_NODE, ARBOREAL_ACCUMULATION_APPARATUS)
                .add(AffinityBlockFamilies.AZALEA.getVariants().values().toArray(new Block[0]));
    }
}
