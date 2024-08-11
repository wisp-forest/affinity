package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.endec.SerializationContext;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.TippedArrowRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TippedArrowRecipe.class)
public class TippedArrowRecipeMixin {

    @Inject(method = "craft(Lnet/minecraft/inventory/RecipeInputInventory;Lnet/minecraft/registry/DynamicRegistryManager;)Lnet/minecraft/item/ItemStack;", at = @At(value = "RETURN", ordinal = 1))
    private void addExtraData(RecipeInputInventory inputInventory, DynamicRegistryManager drm, CallbackInfoReturnable<ItemStack> cir) {
        inputInventory.getStack(1 + inputInventory.getWidth())
                .copyIfPresent(PotionMixture.EXTRA_DATA, SerializationContext.empty(), cir.getReturnValue());
    }
}
