package io.wispforest.affinity.enchantment.impl;

import io.wispforest.affinity.enchantment.template.AbsoluteEnchantment;
import io.wispforest.affinity.enchantment.template.EnchantmentEquipEventReceiver;
import io.wispforest.affinity.misc.AffinityEntityAddon;
import io.wispforest.affinity.misc.LivingEntityTickEvent;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;

public class BastionEnchantment extends AbsoluteEnchantment implements EnchantmentEquipEventReceiver {

    public static final AffinityEntityAddon.DataKey<Boolean> BASTION = AffinityEntityAddon.DataKey.withDefaultConstant(true);

    public BastionEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.ARMOR, Type.ARMOR, 160);
    }

    @Override
    public void onEquip(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        if (!this.slotTypes.contains(slot) || !this.hasCompleteArmor(entity)) return;
        AffinityEntityAddon.createDefaultData(entity, BASTION);
    }

    @Override
    public void onUnequip(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        if (!this.slotTypes.contains(slot)) return;
        AffinityEntityAddon.removeData(entity, BASTION);
    }

    static {
        LivingEntityTickEvent.EVENT.register(entity -> {
            if (!AffinityEntityAddon.hasData(entity, BASTION)) return;

            if (entity.world.getTime() % 10 == 0) {
                entity.addStatusEffect(new StatusEffectInstance(AffinityStatusEffects.BASTION_REGENERATION, 15, 0, true, false));
            }
        });
    }
}
