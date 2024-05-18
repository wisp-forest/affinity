package io.wispforest.affinity.mixin.access;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Invoker("getAllOfType")
    <T extends Recipe<?>> Map<Identifier, T> affinity$getAllOfType(RecipeType<T> type);
}
