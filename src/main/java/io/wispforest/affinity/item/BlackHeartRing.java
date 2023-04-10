package io.wispforest.affinity.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class BlackHeartRing extends TrinketItem {
    public BlackHeartRing() {
        super(AffinityItems.settings(AffinityItemGroup.EQUIPMENT).maxCount(1));
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        return ImmutableMultimap.of(
            EntityAttributes.GENERIC_MAX_HEALTH, new EntityAttributeModifier(uuid, "being goth makes you healthy", 5, EntityAttributeModifier.Operation.ADDITION)
        );
    }
}
