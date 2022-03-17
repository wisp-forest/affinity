package io.wispforest.affinity.mixin;

import io.wispforest.affinity.object.AffinityEnchantments;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public class StyleMixin {

    @Inject(method = "isObfuscated", at = @At("HEAD"), cancellable = true)
    private void weObfuscaten(CallbackInfoReturnable<Boolean> cir) {
        if (MinecraftClient.getInstance().player == null) return;
        final var player = MinecraftClient.getInstance().player;

        if (!EnchantmentHelper.get(player.getEquippedStack(EquipmentSlot.HEAD))
                .containsKey(AffinityEnchantments.CURSE_OF_ILLITERACY)) return;

        cir.setReturnValue(true);
    }

}
