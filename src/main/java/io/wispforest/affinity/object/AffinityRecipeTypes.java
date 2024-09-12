package io.wispforest.affinity.object;

import io.wispforest.affinity.recipe.*;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class AffinityRecipeTypes implements AutoRegistryContainer<RecipeType<?>> {

    public static final RecipeType<PotionMixingRecipe> POTION_MIXING = new SimpleType<>();
    public static final RecipeType<AspenInfusionRecipe> ASPEN_INFUSION = new SimpleType<>();
    public static final RecipeType<SpiritAssimilationRecipe> SPIRIT_ASSIMILATION = new SimpleType<>();
    public static final RecipeType<OrnamentCarvingRecipe> ORNAMENT_CARVING = new SimpleType<>();
    public static final RecipeType<CraftingRecipe> ASSEMBLY = new SimpleType<>();

    public static class Serializers implements AutoRegistryContainer<RecipeSerializer<?>> {

        public static final RecipeSerializer<PotionMixingRecipe> POTION_MIXING = new PotionMixingRecipe.Serializer();
        public static final RecipeSerializer<AspenInfusionRecipe> ASPEN_INFUSION = new AspenInfusionRecipe.Serializer();
        public static final RecipeSerializer<SpiritAssimilationRecipe> SPIRIT_ASSIMILATION = new SpiritAssimilationRecipe.Serializer();
        public static final RecipeSerializer<OrnamentCarvingRecipe> ORNAMENT_CARVING = new OrnamentCarvingRecipe.Serializer();
        public static final RecipeSerializer<ShapedRecipe> ASSEMBLY_SHAPED = new ShapedAssemblyRecipe.Serializer();
        public static final RecipeSerializer<ShapedRecipe> VILLAGER_ARMATURE_ASSEMBLY = new VillagerArmatureAssemblyRecipe.Serializer();
        public static final RecipeSerializer<ShapelessRecipe> ASSEMBLY_SHAPELESS = new ShapelessAssemblyRecipe.Serializer();

        public static final RecipeSerializer<GlowingPotionDyeRecipe> CRAFTING_SPECIAL_GLOWING_POTION_DYE = new SpecialRecipeSerializer<>(GlowingPotionDyeRecipe::new);

        @Override
        public Registry<RecipeSerializer<?>> getRegistry() {
            return Registries.RECIPE_SERIALIZER;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<RecipeSerializer<?>> getTargetFieldType() {
            return (Class<RecipeSerializer<?>>) (Object) RecipeSerializer.class;
        }
    }

    @Override
    public Registry<RecipeType<?>> getRegistry() {
        return Registries.RECIPE_TYPE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<RecipeType<?>> getTargetFieldType() {
        return (Class<RecipeType<?>>) (Object) RecipeType.class;
    }

    private static final class SimpleType<R extends Recipe<?>> implements RecipeType<R> {}
}
