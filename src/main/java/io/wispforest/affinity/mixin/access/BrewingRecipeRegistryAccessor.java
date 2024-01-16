package io.wispforest.affinity.mixin.access;

import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(BrewingRecipeRegistry.class)
public interface BrewingRecipeRegistryAccessor {
    @Accessor("POTION_TYPES")
    static List<Ingredient> affinity$getPotionTypes() {
        throw new UnsupportedOperationException();
    }
}
