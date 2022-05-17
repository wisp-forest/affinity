package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.potion.PotionMixture;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingRecipeRegistry.class)
public class BrewingRecipeRegistryMixin {

    @Inject(method = "hasItemRecipe", at = @At("HEAD"), cancellable = true)
    private static void weDontSplashDoom(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (!MixinHooks.isDoomPotion(input)) return;
        cir.setReturnValue(false);
    }

    @Inject(method = "craft", at = @At("RETURN"))
    private static void addExtraData(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        if (PotionMixture.EXTRA_DATA.maybeIsIn(ingredient.getNbt())) {
            PotionMixture.EXTRA_DATA.put(cir.getReturnValue().getOrCreateNbt(), PotionMixture.EXTRA_DATA.get(input.getNbt()));
        }
    }

}
