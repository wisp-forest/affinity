package io.wispforest.affinity.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.CopyNbtLootFunction;
import net.minecraft.loot.provider.nbt.ContextLootNbtProvider;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

import static io.wispforest.affinity.object.AffinityBlocks.*;
import static io.wispforest.affinity.object.AffinityItems.AZALEA_FLOWERS;

public class AffinityBlockLootTableProvider extends FabricBlockLootTableProvider {

    public AffinityBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(dataOutput, registriesFuture);
    }

    @Override
    public void generate() {
        this.selfDrop(
                BREWING_CAULDRON, COPPER_PLATED_AETHUM_FLUX_NODE, AETHUM_FLUX_CACHE, CREATIVE_AETHUM_FLUX_CACHE, STONE_BANDED_AETHUM_FLUX_NODE, SUNDIAL,
                ARBOREAL_ANNIHILATION_APPARATUS, BLANK_RITUAL_SOCLE, RUDIMENTARY_RITUAL_SOCLE, REFINED_RITUAL_SOCLE, SOPHISTICATED_RITUAL_SOCLE, ASSEMBLY_AUGMENT,
                SPIRIT_INTEGRATION_APPARATUS, RITUAL_SOCLE_COMPOSER, AFFINE_INFUSER, RANTHRACITE_WIRE, CROP_REAPER, WORLD_PIN, SUNDIAL, ARCANE_TREETAP,
                STAFF_PEDESTAL, OUIJA_BOARD, ITEM_TRANSFER_NODE, AETHUM_PROBE, EMERALD_BLOCK, EMERALD_BLOCK, THE_SKY, INVERSION_STONE,
                INFUSED_STONE, MATTER_HARVESTING_HEARTH, ASP_RITE_CORE, FIELD_COHERENCE_MODULATOR, GRAVITON_TRANSDUCER, ETHEREAL_AETHUM_FLUX_INJECTOR,
                LOCAL_DISPLACEMENT_GATEWAY, AZALEA_CHEST
        );

        this.addDrop(AFFINE_CANDLE, candleDrops(AFFINE_CANDLE));

        this.addDrop(BUDDING_AZALEA_LEAVES, block -> this.leavesDrops(block, Blocks.AZALEA, SAPLING_DROP_CHANCE));
        this.addDrop(UNFLOWERING_AZALEA_LEAVES, block -> this.leavesDrops(block, Blocks.AZALEA, SAPLING_DROP_CHANCE));
        this.addDrop(Blocks.FLOWERING_AZALEA_LEAVES, block -> this.leavesDrops(block, Blocks.FLOWERING_AZALEA, SAPLING_DROP_CHANCE).pool(
                LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1.0F))
                        .conditionally(createWithoutShearsOrSilkTouchCondition())
                        .with(this.addSurvivesExplosionCondition(block, ItemEntry.builder(AZALEA_FLOWERS)))
        ));

        this.selfDrop(AZALEA_LOG, AZALEA_WOOD, STRIPPED_AZALEA_LOG, STRIPPED_AZALEA_WOOD, AZALEA_PLANKS);
        this.selfDrop(AZALEA_HANGING_SIGN, AZALEA_WALL_HANGING_SIGN);
        for (var block : AffinityBlockFamilies.AZALEA.getVariants().values()) {
            if (block instanceof DoorBlock) {
                this.addDrop(block, this.doorDrops(block));
            } else {
                this.selfDrop(block);
            }
        }

        this.addDrop(HOLOGRAPHIC_STEREOPTICON, block -> {
            return LootTable.builder().pool(this.addSurvivesExplosionCondition(
                    block,
                    LootPool.builder()
                            .rolls(ConstantLootNumberProvider.create(1.0F))
                            .with(ItemEntry.builder(block).apply(
                                    CopyNbtLootFunction.builder(ContextLootNbtProvider.BLOCK_ENTITY)
                                            .withOperation("{}", "BlockEntityTag")
                            ))
            ));
        });
    }

    private void selfDrop(Block... blocks) {
        for (var block : blocks) this.addDrop(block);
    }
}
