package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.potion.ExtraPotionData;
import io.wispforest.affinity.misc.potion.PotionMixture;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.TippedArrowRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TippedArrowRecipe.class)
public class TippedArrowRecipeMixin {

    @Inject(method = "craft(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;", at = @At(value = "RETURN", ordinal = 1))
    private void addExtraData(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup, CallbackInfoReturnable<ItemStack> cir) {
        ExtraPotionData.copyExtraData(craftingRecipeInput.getStacks().get(1 + craftingRecipeInput.getWidth()), cir.getReturnValue());
    }
}
