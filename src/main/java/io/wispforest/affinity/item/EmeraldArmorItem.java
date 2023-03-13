package io.wispforest.affinity.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityEntityAttributes;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Rarity;

import java.util.UUID;

public class EmeraldArmorItem extends ArmorItem {

    private final Multimap<EntityAttribute, EntityAttributeModifier> modifiers;

    public EmeraldArmorItem(EquipmentSlot slot) {
        super(Material.INSTANCE, slot, AffinityItems.settings(AffinityItemGroup.MAIN).maxCount(1).rarity(Rarity.UNCOMMON));

        this.modifiers = ImmutableMultimap.<EntityAttribute, EntityAttributeModifier>builder().putAll(super.getAttributeModifiers(slot))
                .put(AffinityEntityAttributes.DAMAGE_TAKEN, new EntityAttributeModifier(UUID.randomUUID(), "i hate attributes, season 2", 1, EntityAttributeModifier.Operation.ADDITION))
                .put(AffinityEntityAttributes.KNOCKBACK_SUSCEPTIBILITY, new EntityAttributeModifier(UUID.randomUUID(), "i hate attributes, season 2, part two", 1, EntityAttributeModifier.Operation.ADDITION))
                .build();
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == this.slot ? this.modifiers : super.getAttributeModifiers(slot);
    }

    public enum Material implements ArmorMaterial {
        INSTANCE;

        @Override
        public int getDurability(EquipmentSlot slot) {
            return 69;
        }

        @Override
        public int getProtectionAmount(EquipmentSlot slot) {
            return -5;
        }

        @Override
        public int getEnchantability() {
            return 15;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ITEM_HONEY_BOTTLE_DRINK;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.ofItems(Items.EMERALD);
        }

        @Override
        public String getName() {
            return Affinity.idPlain("emerald");
        }

        @Override
        public float getToughness() {
            return -2;
        }

        @Override
        public float getKnockbackResistance() {
            return -5f;
        }
    }
}
