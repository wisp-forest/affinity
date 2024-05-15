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

public class AssassinsQuiverItem extends TrinketItem {

    private static final UUID MODIFIER_ID = UUID.fromString("845d718d-5469-4eac-a3bd-d96099633fb7");
    private final Multimap<EntityAttribute, EntityAttributeModifier> modifiers;

    public AssassinsQuiverItem() {
        super(AffinityItems.settings().maxCount(1));

        this.modifiers = ImmutableMultimap.of(
                AffinityEntityAttributes.EXTRA_ARROW_DAMAGE, new EntityAttributeModifier(MODIFIER_ID, "a t t r i b u t e s", 3, EntityAttributeModifier.Operation.ADDITION)
        );
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        var slotType = slot.inventory().getSlotType();
        return slotType.getGroup().equals("chest") && slotType.getName().equals("back")
                ? this.modifiers
                : super.getModifiers(stack, slot, entity, uuid);
    }
}
