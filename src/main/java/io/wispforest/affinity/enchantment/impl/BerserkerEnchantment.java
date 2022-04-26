package io.wispforest.affinity.enchantment.impl;

import io.wispforest.affinity.enchantment.template.AbsoluteEnchantment;
import io.wispforest.affinity.enchantment.template.EnchantmentEquipEventReceiver;
import io.wispforest.affinity.misc.AffinityEntityAddon;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class BerserkerEnchantment extends AbsoluteEnchantment implements EnchantmentEquipEventReceiver {

    private static final EntityAttributeModifier HEALTH_ADDITION = new EntityAttributeModifier(UUID.fromString("a05c1e85-cfaa-49b2-a5f2-21962e769115"),
            "Berserker Health Boost", 10, EntityAttributeModifier.Operation.ADDITION);

    public static final AffinityEntityAddon.DataKey<Boolean> BERSERK_KEY = AffinityEntityAddon.DataKey.withDefaultConstant(false);

    public BerserkerEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.ARMOR, Type.ARMOR, 343);
    }

    @Override
    public void onEquip(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        if (!this.slotTypes.contains(slot)) return;

        if (this.hasCompleteArmor(entity)) {
            AffinityEntityAddon.setData(entity, BERSERK_KEY, true);

            if (!this.healthAttribute(entity).hasModifier(HEALTH_ADDITION)) {
                this.healthAttribute(entity).addTemporaryModifier(HEALTH_ADDITION);
            }
        }
    }

    @Override
    public void onUnequip(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        if (!this.slotTypes.contains(slot)) return;

        if (!this.hasCompleteArmor(entity)) {
            AffinityEntityAddon.setData(entity, BERSERK_KEY, false);

            this.healthAttribute(entity).removeModifier(HEALTH_ADDITION);
            entity.damage(DamageSource.OUT_OF_WORLD, Float.MIN_NORMAL);
        }
    }

    private EntityAttributeInstance healthAttribute(LivingEntity entity) {
        return entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
    }
}
