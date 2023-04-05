package io.wispforest.affinity.enchantment.template;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.*;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;

public abstract class AbsoluteEnchantment extends AffinityEnchantment {

    protected final Type type;
    protected final int nameHue;

    protected AbsoluteEnchantment(Rarity weight, EnchantmentTarget target, Type type, int nameHue) {
        super(weight, target, type.slots);
        this.type = type;
        this.nameHue = nameHue;
    }

    public boolean hasCompleteArmor(LivingEntity entity) {
        if (type != Type.ARMOR) throw new IllegalStateException("hasCompleteArmor() called on non-armor enchantment");
        final var equipment = this.getEquipment(entity);
        if (equipment.size() != 4) return false;

        for (var stack : equipment.values()) {
            if (EnchantmentHelper.getLevel(this, stack) < 1) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Text getName(int level) {
        final var name = Language.getInstance().get(this.getTranslationKey()).toCharArray();
        final var text = MutableText.of(new PhantomTranslatableText(this.getTranslationKey()));

        float hue = this.nameHue / 360f;
        float lightness = 90;

        int padding = 35;
        int highlightLetter = (int) Math.round(System.currentTimeMillis() / 80d % (name.length + padding)) - padding / 2;

        for (int i = 0; i < name.length; i++) {
            int highlightDistance = Math.abs(highlightLetter - i);
            float effectiveLightness = Math.max(52, lightness - highlightDistance * 7) / 100;

            text.append(Text.literal(String.valueOf(name[i]))
                    .setStyle(Style.EMPTY.withColor(MathHelper.hsvToRgb(hue, 0.5f, effectiveLightness))));
        }

        return text;
    }

    public int nameHue() {
        return nameHue;
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        return false;
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return false;
    }

    public enum Type {
        ITEM(EquipmentSlot.MAINHAND),
        ARMOR(EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);

        public final EquipmentSlot[] slots;

        Type(EquipmentSlot... slots) {
            this.slots = slots;
        }
    }

    private static class PhantomTranslatableText extends TranslatableTextContent {

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
