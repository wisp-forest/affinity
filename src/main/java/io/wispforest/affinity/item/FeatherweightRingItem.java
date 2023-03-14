package io.wispforest.affinity.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import io.wispforest.affinity.object.AffinityEntityAttributes;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class FeatherweightRingItem extends TrinketItem {

    private static final UUID KNOCKBACK_MODIFIER_ID = UUID.fromString("59821def-8960-42a2-bd2a-e9e8c08abf00");
    private static final UUID FALL_MODIFIER_ID = UUID.fromString("557d97da-d830-41ea-b86d-5e379fd27468");

    private final Multimap<EntityAttribute, EntityAttributeModifier> modifiers;

    public FeatherweightRingItem() {
        super(AffinityItems.settings(AffinityItemGroup.EQUIPMENT).maxCount(1));

        this.modifiers = ImmutableMultimap.of(
                AffinityEntityAttributes.KNOCKBACK_SUSCEPTIBILITY, new EntityAttributeModifier(KNOCKBACK_MODIFIER_ID, "i still hate attributes, season 3", .75, EntityAttributeModifier.Operation.ADDITION),
                AffinityEntityAttributes.FALL_RESISTANCE, new EntityAttributeModifier(FALL_MODIFIER_ID, "i still hate attributes, season 3, episode²", 3, EntityAttributeModifier.Operation.ADDITION)
        );
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        var slotType = slot.inventory().getSlotType();
        return slotType.getGroup().equals("hand") && slotType.getName().equals("ring")
                ? this.modifiers
                : super.getModifiers(stack, slot, entity, uuid);
    }
}
