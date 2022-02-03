package io.wispforest.affinity.misc.potion;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import org.jetbrains.annotations.Nullable;

public class GlowingPotion extends Potion {

    public GlowingPotion(@Nullable String baseName, StatusEffectInstance... effects) {
        super(baseName, effects);
    }
}
