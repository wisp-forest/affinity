// TODO: port this.

//package io.wispforest.affinity.enchantment.template;
//
//import net.minecraft.enchantment.Enchantment;
//import net.minecraft.enchantment.EnchantmentTarget;
//import net.minecraft.entity.EquipmentSlot;
//
//import java.util.Arrays;
//import java.util.EnumSet;
//
//public abstract class AffinityEnchantment extends Enchantment {
//
//    protected final EnumSet<EquipmentSlot> slotTypes;
//
//    protected AffinityEnchantment(Rarity weight, EnchantmentTarget type, EquipmentSlot... slotTypes) {
//        super(weight, type, slotTypes);
//        this.slotTypes = EnumSet.copyOf(Arrays.asList(slotTypes));
//    }
//
//    @Override
//    public boolean isAvailableForRandomSelection() {
//        return false;
//    }
//
//    @Override
//    public boolean isAvailableForEnchantedBookOffer() {
//        return false;
//    }
//}
