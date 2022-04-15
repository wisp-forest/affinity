package io.wispforest.affinity.object;

import io.wispforest.affinity.misc.recipe.*;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.registry.Registry;

public class AffinityRecipeTypes implements AutoRegistryContainer<RecipeType<?>> {

    public static final SimpleType<PotionMixingRecipe> POTION_MIXING = new SimpleType<>();
    public static final SimpleType<AspenInfusionRecipe> ASPEN_INFUSION = new SimpleType<>();
    public static final SimpleType<AberrantCallingRecipe> ABERRANT_CALLING = new SimpleType<>();
    public static final SimpleType<RitualSocleComposerConversionRecipe> RITUAL_SOCLE_COMPOSER_CONVERSION = new SimpleType<>();

    public static class Serializers implements AutoRegistryContainer<RecipeSerializer<?>> {

        public static final RecipeSerializer<PotionMixingRecipe> POTION_MIXING = PotionMixingRecipeSerializer.INSTANCE;
        public static final RecipeSerializer<AspenInfusionRecipe> ASPEN_INFUSION = AspenInfusionRecipe.Serializer.INSTANCE;
        public static final RecipeSerializer<AberrantCallingRecipe> ABERRANT_CALLING = AberrantCallingRecipe.Serializer.INSTANCE;
        public static final RecipeSerializer<RitualSocleComposerConversionRecipe> RITUAL_SOCLE_COMPOSER_CONVERSION
                = RitualSocleComposerConversionRecipe.Serializer.INSTANCE;

        @Override
        public Registry<RecipeSerializer<?>> getRegistry() {
            return Registry.RECIPE_SERIALIZER;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<RecipeSerializer<?>> getTargetFieldType() {
            return (Class<RecipeSerializer<?>>) (Object) RecipeSerializer.class;
        }
    }

    @Override
    public Registry<RecipeType<?>> getRegistry() {
        return Registry.RECIPE_TYPE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<RecipeType<?>> getTargetFieldType() {
        return (Class<RecipeType<?>>) (Object) RecipeType.class;
    }

    private static final class SimpleType<R extends Recipe<?>> implements RecipeType<R> {}
}
