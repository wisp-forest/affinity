package io.wispforest.affinity.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableTextContent;

import java.util.Optional;

public class AbsoluteEnchantmentLogic {
    public static boolean hasCompleteArmor(LivingEntity entity, RegistryEntry<Enchantment> enchantment) {
//        if (enchantment.value().type != Type.ARMOR) throw new IllegalStateException("hasCompleteArmor() called on non-armor enchantment");

        final var equipment = enchantment.value().getEquipment(entity);
        if (equipment.size() != 4) return false;

        for (var stack : equipment.values()) {
            if (EnchantmentHelper.getLevel(enchantment, stack) < 1) {
                return false;
            }
        }

        return true;
    }

    public static class PhantomTranslatableText extends TranslatableTextContent {

        public PhantomTranslatableText(String key) {
            super(key, null, new Object[0]);
        }

        @Override
        public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
            return Optional.empty();
        }

        @Override
        public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
            return Optional.empty();
        }
    }
}
