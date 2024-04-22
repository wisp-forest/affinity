package io.wispforest.affinity.client;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.item.IridescenceWandItem;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class AffinityModelPredicateProviders {

    public static final ClampedModelPredicateProvider IRIDESCENCE_WAND_MODE =
            (stack, world, entity, seed) -> stack.get(IridescenceWandItem.MODE_KEY).ordinal() / (float) IridescenceWandItem.Mode.values().length;

    public static void applyDefaults() {
        ModelPredicateProviderRegistry.register(AffinityItems.EMERALD_WAND_OF_IRIDESCENCE, Affinity.id("mode"), IRIDESCENCE_WAND_MODE);
        ModelPredicateProviderRegistry.register(AffinityItems.SAPPHIRE_WAND_OF_IRIDESCENCE, Affinity.id("mode"), IRIDESCENCE_WAND_MODE);

        ModelPredicateProviderRegistry.register(AffinityItems.AZALEA_BOW, new Identifier("pulling"), (ClampedModelPredicateProvider) ModelPredicateProviderRegistry.get(Items.BOW, new Identifier("pulling")));
        ModelPredicateProviderRegistry.register(AffinityItems.AZALEA_BOW, new Identifier("pull"), (stack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                return entity.getActiveItem() != stack ? 0.0F : (float) (stack.getMaxUseTime() - entity.getItemUseTimeLeft()) / 10.0F;
            }
        });
    }

}
