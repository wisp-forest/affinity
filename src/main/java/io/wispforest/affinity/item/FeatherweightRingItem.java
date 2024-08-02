package io.wispforest.affinity.item;

import dev.emi.trinkets.api.TrinketItem;
import dev.emi.trinkets.api.TrinketsAttributeModifiersComponent;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityEntityAttributes;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registries;

public class FeatherweightRingItem extends TrinketItem {

    public FeatherweightRingItem() {
        super(AffinityItems.settings().maxCount(1).component(
                TrinketsAttributeModifiersComponent.TYPE,
                TrinketsAttributeModifiersComponent.builder()
                        .add(
                                Registries.ATTRIBUTE.getEntry(AffinityEntityAttributes.KNOCKBACK_SUSCEPTIBILITY),
                                new EntityAttributeModifier(Affinity.id("featherweight_ring_knockback_susceptibility"), .75, EntityAttributeModifier.Operation.ADD_VALUE),
                                "hand/ring"
                        )
                        .add(
                                EntityAttributes.GENERIC_SAFE_FALL_DISTANCE,
                                new EntityAttributeModifier(Affinity.id("featherweight_ring_safe_fall_distance"), 3, EntityAttributeModifier.Operation.ADD_VALUE),
                                "hand/ring"
                        ).build()
        ));
    }
}
