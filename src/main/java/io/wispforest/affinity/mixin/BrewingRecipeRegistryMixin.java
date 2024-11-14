package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.potion.ExtraPotionData;
import io.wispforest.affinity.misc.potion.PotionUtil;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingRecipeRegistry.class)
public abstract class BrewingRecipeRegistryMixin {

    @Inject(method = "hasItemRecipe", at = @At("HEAD"), cancellable = true)
    private void weDontSplashDoom(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (!MixinHooks.isDoomPotion(input)) return;
        cir.setReturnValue(false);
    }

    @Inject(method = "craft", at = @At("RETURN"))
    private void addExtraData(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        ExtraPotionData.copyExtraData(input, cir.getReturnValue());
    }

    @Inject(method = "isValidIngredient", at = @At("HEAD"), cancellable = true)
    private void injectProwessPotionIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (affinity$isStrengthPotion(stack)) {
            cir.setReturnValue(true);
            return;
        }

        if (MixinHooks.isMistInfusion(stack, null)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasRecipe", at = @At("HEAD"), cancellable = true)
    private void injectProwessPotionRecipe(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (affinity$isStrengthPotion(input) && affinity$isStrengthPotion(ingredient)) {
            cir.setReturnValue(true);
            return;
        }

        if (MixinHooks.isMistInfusion(ingredient, input)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private void injectProwessPotionResult(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        if (affinity$isStrengthPotion(input) && affinity$isStrengthPotion(ingredient)) {
            cir.setReturnValue(AffinityItems.makePotionOfInfiniteProwess());
        }

        if (MixinHooks.isMistInfusion(ingredient, input)) {
            cir.setReturnValue(MixinHooks.craftMistInfusion(ingredient, input));
        }
    }

    @Unique
    private static boolean affinity$isStrengthPotion(ItemStack stack) {
        return stack.isOf(Items.POTION) && PotionUtil.getPotion(stack) == Potions.LONG_STRENGTH.value();
    }

}
