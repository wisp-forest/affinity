package io.wispforest.affinity.mixin;

import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(method = "getEnchantmentsComponentType", at = @At("HEAD"), cancellable = true)
    private static void injectGemEnchantments(ItemStack stack, CallbackInfoReturnable<ComponentType<ItemEnchantmentsComponent>> cir) {
        if (stack.isOf(AffinityItems.RESPLENDENT_GEM))
            cir.setReturnValue(DataComponentTypes.STORED_ENCHANTMENTS);
    }

}
