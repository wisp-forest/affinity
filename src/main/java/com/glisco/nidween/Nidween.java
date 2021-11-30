package com.glisco.nidween;

import com.glisco.nidween.registries.NidweenBlocks;
import com.glisco.nidween.registries.NidweenStatusEffects;
import com.glisco.nidween.util.recipe.PotionMixingRecipe;
import com.glisco.nidween.util.recipe.PotionMixingRecipeSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Nidween implements ModInitializer {

    public static final String MOD_ID = "nidween";

    @Override
    public void onInitialize() {
        NidweenBlocks.register();
        NidweenStatusEffects.register();

        Registry.register(Registry.RECIPE_TYPE, PotionMixingRecipe.Type.ID, PotionMixingRecipe.Type.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, PotionMixingRecipeSerializer.ID, PotionMixingRecipeSerializer.INSTANCE);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

}
