package io.wispforest.affinity.misc.potion;

import io.wispforest.endec.CodecUtils;
import io.wispforest.endec.impl.KeyedEndec;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

public class GlowingPotion extends Potion {

    public static final KeyedEndec<DyeColor> COLOR_KEY = CodecUtils.ofCodec(DyeColor.CODEC).keyed("Color", (DyeColor) null);

    public GlowingPotion(@Nullable String baseName, StatusEffectInstance... effects) {
        super(baseName, effects);
    }
}
