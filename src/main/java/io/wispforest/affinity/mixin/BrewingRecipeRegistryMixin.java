package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
        input.copyIfPresent(PotionMixture.EXTRA_DATA, cir.getReturnValue());
    }

    @Inject(method = "isValidIngredient", at = @At("HEAD"), cancellable = true)
    private static void injectProwessPotionIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (affinity$isStrengthPotion(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasRecipe", at = @At("HEAD"), cancellable = true)
    private static void injectProwessPotionRecipe(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (affinity$isStrengthPotion(input) && affinity$isStrengthPotion(ingredient)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private static void injectProwessPotionResult(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<ItemStack> cir) {
        if (affinity$isStrengthPotion(input) && affinity$isStrengthPotion(ingredient)) {
            cir.setReturnValue(AffinityItems.makePotionOfInfiniteProwess());
        }
    }

    @Unique
    private static boolean affinity$isStrengthPotion(ItemStack stack) {
        return stack.isOf(Items.POTION) && PotionUtil.getPotion(stack) == Potions.LONG_STRENGTH;
    }

}
