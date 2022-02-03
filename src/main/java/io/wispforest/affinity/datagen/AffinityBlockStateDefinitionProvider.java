package io.wispforest.affinity.datagen;

import io.wispforest.affinity.Affinity;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockStateDefinitionProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.model.*;
import net.minecraft.item.Item;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

import java.util.Optional;

import static io.wispforest.affinity.init.AffinityBlocks.*;
import static io.wispforest.affinity.init.AffinityItems.*;

public class AffinityBlockStateDefinitionProvider extends FabricBlockStateDefinitionProvider {

    private static final Model IRIDESCENCE_WAND_MODEL = new Model(Optional.of(Affinity.id("item/iridescence_wand")), Optional.empty(), TextureKey.LAYER0);

    public AffinityBlockStateDefinitionProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        generator.registerCubeAllModelTexturePool(AZALEA_PLANKS).family(AffinityBlockFamilies.AZALEA);
        generator.registerLog(AZALEA_LOG).log(AZALEA_LOG).wood(AZALEA_WOOD);
        generator.registerLog(STRIPPED_AZALEA_LOG).log(STRIPPED_AZALEA_LOG).wood(STRIPPED_AZALEA_WOOD);
        parentedItem(generator, AZALEA_LOG, AZALEA_WOOD, STRIPPED_AZALEA_LOG, STRIPPED_AZALEA_WOOD);
        parentedItem(generator, AZALEA_PLANKS, AZALEA_FENCE_GATE, AZALEA_PRESSURE_PLATE);

        var variantMap = BlockStateVariantMap.create(Properties.AGE_2)
                .register(integer -> {
                    var id = generator.createSubModel(BUDDING_AZALEA_LEAVES, "_" + integer, Models.CUBE_ALL, Texture::all);
                    return BlockStateVariant.create().put(VariantSettings.MODEL, id);
                });
        generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(BUDDING_AZALEA_LEAVES).coordinate(variantMap));
        generator.registerParentedItemModel(BUDDING_AZALEA_LEAVES.asItem(), ModelIds.getBlockSubModelId(BUDDING_AZALEA_LEAVES, "_0"));

        cubeAllWithParentedItem(generator, UNFLOWERING_AZALEA_LEAVES);
        cubeAllWithParentedItem(generator, PECULIAR_CLUMP);

        simpleStateWithGeneratedItem(generator, BREWING_CAULDRON);

        simpleStateWithParentedItem(generator, COPPER_PLATED_AETHUM_FLUX_NODE);
        simpleStateWithParentedItem(generator, STONE_BANDED_AETHUM_FLUX_NODE);
        simpleStateWithGeneratedItem(generator, SUNDIAL);
        simpleStateWithParentedItem(generator, ARBOREAL_ACCUMULATION_APPARATUS);

    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
        generated(generator, AZALEA_FLOWERS, AETHUM_FLUX_BOTTLE, AZALEA_BOAT,
                MILDLY_ATTUNED_AMETHYST_SHARD, FAIRLY_ATTUNED_AMETHYST_SHARD, GREATLY_ATTUNED_AMETHYST_SHARD);

        generatedWithTexture(generator, Affinity.id("item/wisp_matter"), INERT_WISP_MATTER, WISE_WISP_MATTER, VICIOUS_WISP_MATTER);

        iridescenceWand(generator, EMERALD_WAND_OF_IRIDESCENCE, SAPPHIRE_WAND_OF_IRIDESCENCE);

        generator.register(GEOLOGICAL_RESONATOR, Models.HANDHELD);
    }

    private void generated(ItemModelGenerator generator, Item... items) {
        for (var item : items) {
            generator.register(item, Models.GENERATED);
        }
    }

    private void iridescenceWand(ItemModelGenerator generator, Item... items) {
        for (var item : items) {
            generator.register(item, IRIDESCENCE_WAND_MODEL);
        }
    }

    private void generatedWithTexture(ItemModelGenerator generator, Identifier texture, Item... items) {
        for (var item : items) {
            Models.GENERATED.upload(ModelIds.getItemModelId(item), Texture.layer0(texture), generator.writer);
        }
    }

    private void parentedItem(BlockStateModelGenerator generator, Block... blocks) {
        for (Block block : blocks) {
            generator.registerParentedItemModel(block.asItem(), ModelIds.getBlockModelId(block));
        }
    }

    private void simpleStateWithGeneratedItem(BlockStateModelGenerator generator, Block block) {
        generator.registerSimpleState(block);
        generator.registerItemModel(block.asItem());
    }

    private void simpleStateWithParentedItem(BlockStateModelGenerator generator, Block block) {
        generator.registerSimpleState(block);
        var blockModelId = ModelIds.getBlockModelId(block);
        generator.registerParentedItemModel(block.asItem(), blockModelId);
    }

    private void cubeAllWithParentedItem(BlockStateModelGenerator generator, Block block) {
        generator.registerSimpleCubeAll(block);
        generator.registerParentedItemModel(block.asItem(), ModelIds.getBlockModelId(block));
    }
}
