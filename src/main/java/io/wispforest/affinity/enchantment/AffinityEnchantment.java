package io.wispforest.affinity.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

import java.util.Arrays;
import java.util.List;

public abstract class AffinityEnchantment extends Enchantment {

    protected final List<EquipmentSlot> slotTypes;

    protected AffinityEnchantment(Rarity weight, EnchantmentTarget type, EquipmentSlot... slotTypes) {
        super(weight, type, slotTypes);
        this.slotTypes = Arrays.asList(slotTypes);
    }
}
