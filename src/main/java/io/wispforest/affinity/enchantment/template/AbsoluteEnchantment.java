// TODO: port this to tags.

//package io.wispforest.affinity.enchantment.template;
//
//import net.minecraft.enchantment.Enchantment;
//import net.minecraft.enchantment.EnchantmentHelper;
//import net.minecraft.enchantment.EnchantmentTarget;
//import net.minecraft.entity.EquipmentSlot;
//import net.minecraft.entity.LivingEntity;
//import net.minecraft.text.*;
//import net.minecraft.util.Language;
//import net.minecraft.util.math.MathHelper;
//
//import java.util.Optional;
//
//public abstract class AbsoluteEnchantment extends AffinityEnchantment {
//
//    protected final Type type;
//    protected final int nameHue;
//
//    protected AbsoluteEnchantment(Rarity weight, EnchantmentTarget target, Type type, int nameHue) {
//        super(weight, target, type.slots);
//        this.type = type;
//        this.nameHue = nameHue;
//    }
//
//
//    @Override
//    protected boolean canAccept(Enchantment other) {
//        return false;
//    }
//
//    @Override
//    public boolean isAvailableForEnchantedBookOffer() {
//        return false;
//    }
//
//    public enum Type {
//        ITEM(EquipmentSlot.MAINHAND),
//        ARMOR(EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
//
//        public final EquipmentSlot[] slots;
//
//        Type(EquipmentSlot... slots) {
//            this.slots = slots;
//        }
//    }
//

//}
