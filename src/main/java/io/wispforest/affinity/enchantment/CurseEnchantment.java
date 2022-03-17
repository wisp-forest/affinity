package io.wispforest.affinity.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

import java.util.Arrays;
import java.util.List;

public class CurseEnchantment extends Enchantment {

    protected final List<EquipmentSlot> slotTypes;

    protected CurseEnchantment(Rarity weight, EnchantmentTarget type, EquipmentSlot... slotTypes) {
        super(weight, type, slotTypes);
        this.slotTypes = Arrays.asList(slotTypes);
    }

    @Override
    public boolean isCursed() {
        return true;
    }
}
