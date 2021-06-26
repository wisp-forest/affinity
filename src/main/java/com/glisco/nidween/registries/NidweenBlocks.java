package com.glisco.nidween.registries;

import com.glisco.nidween.Nidween;
import com.glisco.nidween.block.BrewingCauldronBlock;
import com.glisco.nidween.block.BrewingCauldronBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public class NidweenBlocks {

    public static final Block BREWING_CAULDRON_BLOCK = new BrewingCauldronBlock();

    public static void register() {
        Registry.register(Registry.BLOCK, Nidween.id("brewing_cauldron"), BREWING_CAULDRON_BLOCK);

        BlockEntityTypes.register();
    }

    public static class BlockEntityTypes {

        public static BlockEntityType<BrewingCauldronBlockEntity> BREWING_CAULDRON = FabricBlockEntityTypeBuilder.create(BrewingCauldronBlockEntity::new, BREWING_CAULDRON_BLOCK).build();

        private static void register() {
            Registry.register(Registry.BLOCK_ENTITY_TYPE, Nidween.id("brewing_cauldron"), BREWING_CAULDRON);
        }

    }

}
