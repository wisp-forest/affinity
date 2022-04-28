package io.wispforest.affinity.enchantment.impl;

import io.wispforest.affinity.enchantment.template.AbsoluteEnchantment;
import io.wispforest.affinity.enchantment.template.EnchantmentEquipEventReceiver;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class BastionEnchantment extends AbsoluteEnchantment implements EnchantmentEquipEventReceiver {

    private static final EntityAttributeModifier DAMAGE_REDUCTION = new EntityAttributeModifier(UUID.fromString("0087b04b-a20e-4eae-9c07-f1384451a408"),
            "Bastion Attack Damage Penalty", -.5, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public BastionEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.ARMOR, Type.ARMOR, 160);
    }

    @Override
    public void onEquip(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        if (!this.slotTypes.contains(slot)) return;

        if (this.hasCompleteArmor(entity) && !damageAttribute(entity).hasModifier(DAMAGE_REDUCTION)) {
            damageAttribute(entity).addTemporaryModifier(DAMAGE_REDUCTION);
        }
    }

    @Override
    public void onUnequip(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        if (!this.slotTypes.contains(slot)) return;

        if (!this.hasCompleteArmor(entity)) {
            damageAttribute(entity).removeModifier(DAMAGE_REDUCTION);
        }
    }

    private EntityAttributeInstance damageAttribute(LivingEntity entity) {
        return entity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
    }
}
