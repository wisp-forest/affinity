package io.wispforest.affinity.mixin;

import io.wispforest.affinity.item.ResplendentGemItem;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @ModifyVariable(method = "get", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;fromNbt(Lnet/minecraft/nbt/NbtList;)Ljava/util/Map;", shift = At.Shift.BEFORE))
    private static NbtList injectGemEnchantments(NbtList value, ItemStack stack) {
        return stack.isOf(AffinityItems.RESPLENDENT_GEM) ? ResplendentGemItem.getEnchantmentNbt(stack) : value;
    }

}
