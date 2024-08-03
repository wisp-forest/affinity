package io.wispforest.affinity.datagen;

import io.wispforest.affinity.object.AffinityEntities;
import io.wispforest.affinity.object.AffinityItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemConvertible;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.EnchantedCountIncreaseLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class AffinityEntityLootTableProvider extends SimpleFabricLootTableProvider {

    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture;

    public AffinityEntityLootTableProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup, LootContextTypes.ENTITY);
        this.registryLookupFuture = registryLookup;
    }

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> consumer) {
        forEntity(consumer, AffinityEntities.INERT_WISP, itemWithLooting(AffinityItems.INERT_WISP_MATTER, 3));
        forEntity(consumer, AffinityEntities.VICIOUS_WISP, itemWithLooting(AffinityItems.VICIOUS_WISP_MATTER, 1));
        forEntity(consumer, AffinityEntities.WISE_WISP, itemWithLooting(AffinityItems.WISE_WISP_MATTER, 2));
    }

    private LootTable.Builder itemWithLooting(ItemConvertible item, int maxCount) {
        return LootTable.builder().pool(LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(ItemEntry.builder(item)
                        .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, maxCount)))
                        .apply(EnchantedCountIncreaseLootFunction.builder(registryLookupFuture.resultNow(), UniformLootNumberProvider.create(0.0F, 1.0F)))
                )
        );
    }

    private void forEntity(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> consumer, EntityType<?> type, LootTable.Builder table) {
        consumer.accept(type.getLootTableId(), table);
    }
}
