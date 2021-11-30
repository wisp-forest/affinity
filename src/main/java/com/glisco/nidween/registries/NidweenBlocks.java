package com.glisco.nidween.registries;

import com.glisco.nidween.Nidween;
import com.glisco.nidween.block.*;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class NidweenBlocks {

    public static final Block BREWING_CAULDRON = new BrewingCauldronBlock();
    public static final Block COPPER_PLATED_AETHER_FLUX_NODE = new CopperPlatedAetherFluxNodeBlock();
    public static final Block STONE_BANDED_AETHER_FLUX_NODE = new StoneBandedAetherFluxNodeBlock();

    public static void register() {
        registerBlockAndItem("brewing_cauldron", BREWING_CAULDRON);
        registerBlockAndItem("copper_plated_aether_flux_node", COPPER_PLATED_AETHER_FLUX_NODE);
        registerBlockAndItem("stone_banded_aether_flux_node", STONE_BANDED_AETHER_FLUX_NODE);

        BlockEntityTypes.register();
    }

    private static void registerBlockAndItem(String name, Block block) {
        registerBlockAndItem(name, block, new Item.Settings());
    }

    private static void registerBlockAndItem(String name, Block block, Item.Settings settings) {
        Registry.register(Registry.BLOCK, Nidween.id(name), block);
        Registry.register(Registry.ITEM, Nidween.id(name), new BlockItem(block, settings));
    }

    public static class BlockEntityTypes {

        public static BlockEntityType<BrewingCauldronBlockEntity> BREWING_CAULDRON = FabricBlockEntityTypeBuilder.create(BrewingCauldronBlockEntity::new, NidweenBlocks.BREWING_CAULDRON).build();
        public static BlockEntityType<AetherFluxNodeBlockEntity> AETHER_FLUX_NODE = FabricBlockEntityTypeBuilder.create(AetherFluxNodeBlockEntity::new, NidweenBlocks.COPPER_PLATED_AETHER_FLUX_NODE, NidweenBlocks.STONE_BANDED_AETHER_FLUX_NODE).build();

        private static void register() {
            Registry.register(Registry.BLOCK_ENTITY_TYPE, Nidween.id("brewing_cauldron"), BREWING_CAULDRON);
            Registry.register(Registry.BLOCK_ENTITY_TYPE, Nidween.id("aether_flux_node"), AETHER_FLUX_NODE);
        }

    }

}
