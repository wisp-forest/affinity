package io.wispforest.affinity.mixin;

import io.wispforest.affinity.enchantment.AbsoluteEnchantmentLogic;
import io.wispforest.affinity.object.AffinityEnchantmentEffectComponents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private static void injectAbsolute(RegistryEntry<Enchantment> enchantment, int level, CallbackInfoReturnable<Text> cir) {
        var nameHue = enchantment.value().effects().get(AffinityEnchantmentEffectComponents.ABSOLUTE_NAME_HUE);
        if (nameHue == null) return;

        var description = enchantment.value().description();
        if (!(description.getContent() instanceof TranslatableTextContent translatable)) return;

        final var name = Language.getInstance().get(translatable.getKey()).toCharArray();
        final var text = MutableText.of(new AbsoluteEnchantmentLogic.PhantomTranslatableText(translatable.getKey()));

        float hue = nameHue / 360f;
        float lightness = 90;

        int padding = 35;
        int highlightLetter = (int) Math.round(System.currentTimeMillis() / 80d % (name.length + padding)) - padding / 2;

        for (int i = 0; i < name.length; i++) {
            int highlightDistance = Math.abs(highlightLetter - i);
            float effectiveLightness = Math.max(52, lightness - highlightDistance * 7) / 100;

            text.append(Text.literal(String.valueOf(name[i]))
                    .setStyle(Style.EMPTY.withColor(MathHelper.hsvToRgb(hue, 0.5f, effectiveLightness))));
        }

        cir.setReturnValue(text);
    }
}
