package io.wispforest.affinity.client;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.item.IridescenceWandItem;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.item.UnclampedModelPredicateProvider;

public class AffinityModelPredicateProviders {

    public static final UnclampedModelPredicateProvider IRIDESCENCE_WAND_MODE =
            (stack, world, entity, seed) -> stack.get(IridescenceWandItem.MODE).ordinal();

    public static void applyDefaults() {
        ModelPredicateProviderRegistry.register(AffinityItems.EMERALD_WAND_OF_IRIDESCENCE, Affinity.id("mode"), IRIDESCENCE_WAND_MODE);
        ModelPredicateProviderRegistry.register(AffinityItems.SAPPHIRE_WAND_OF_IRIDESCENCE, Affinity.id("mode"), IRIDESCENCE_WAND_MODE);
    }

}
