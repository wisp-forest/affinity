package io.wispforest.affinity.misc.potion;


import io.wispforest.affinity.Affinity;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.serialization.CodecUtils;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

public class GlowingPotion extends Potion {

    public static final ComponentType<DyeColor> COLOR = Affinity.component("glowing_potion_color", CodecUtils.toEndec(DyeColor.CODEC));

    public GlowingPotion(@Nullable String baseName, StatusEffectInstance... effects) {
        super(baseName, effects);
    }
}
