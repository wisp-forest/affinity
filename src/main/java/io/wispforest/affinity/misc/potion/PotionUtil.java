package io.wispforest.affinity.misc.potion;

import com.google.common.collect.ImmutableList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.*;

// Insta-legacy code am i rite
// - Basique
public final class PotionUtil {
    private PotionUtil() { }

    public static Potion getPotion(ItemStack stack) {
        var contents = stack.get(DataComponentTypes.POTION_CONTENTS);

        if (contents == null) return Potions.WATER.value(); // this used to be Potions.EMPTY

        return contents.potion().orElse(Potions.WATER).value();
    }

    public static List<StatusEffectInstance> getPotionEffects(ItemStack stack) {
        var contents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (contents == null) return List.of();

        var builder = ImmutableList.<StatusEffectInstance>builder();
        contents.forEachEffect(builder::add);
        return builder.build();
    }

    public static ItemStack setPotion(ItemStack stack, Potion potion) {
        stack.apply(
            DataComponentTypes.POTION_CONTENTS,
            PotionContentsComponent.DEFAULT,
            Registries.POTION.getEntry(potion),
            PotionContentsComponent::with
        );
        
        return stack;
    }

    public static ItemStack setCustomPotionEffects(ItemStack stack, Collection<StatusEffectInstance> effects) {
        if (effects.isEmpty()) return stack;

        stack.apply(
            DataComponentTypes.POTION_CONTENTS,
            PotionContentsComponent.DEFAULT,
            contents -> new PotionContentsComponent(contents.potion(), contents.customColor(), List.copyOf(effects))
        );

        return stack;
    }

    public static void buildTooltip(List<StatusEffectInstance> statusEffects, List<Text> tooltip, float durationMultiplier, float tickRate) {
        PotionContentsComponent.buildTooltip(statusEffects, tooltip::add, durationMultiplier, tickRate);
    }
}
