package io.wispforest.affinity.misc.potion;

import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

public class GlowingPotion extends Potion {

    public static final NbtKey<DyeColor> COLOR_KEY = new NbtKey<>(
            "Color",
            NbtKey.Type.STRING.then(s -> DyeColor.byName(s, null), DyeColor::asString)
    );

    public GlowingPotion(@Nullable String baseName, StatusEffectInstance... effects) {
        super(baseName, effects);
    }
}
