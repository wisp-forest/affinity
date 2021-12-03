package com.glisco.nidween;

import com.glisco.nidween.item.NidweenItemGroup;
import com.glisco.nidween.registries.NidweenBlocks;
import com.glisco.nidween.registries.NidweenItems;
import com.glisco.nidween.registries.NidweenStatusEffects;
import com.glisco.nidween.util.recipe.PotionMixingRecipe;
import com.glisco.nidween.util.recipe.PotionMixingRecipeSerializer;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Nidween implements ModInitializer {

    public static final String MOD_ID = "nidween";
    public static final OwoItemGroup NIDWEEN_GROUP = new NidweenItemGroup(id("nidween"));

    @Override
    public void onInitialize() {
        FieldRegistrationHandler.register(NidweenBlocks.class, MOD_ID, false);
        FieldRegistrationHandler.register(NidweenBlocks.Entities.class, MOD_ID, false);

        FieldRegistrationHandler.register(NidweenItems.class, MOD_ID, false);

        NidweenStatusEffects.register();

        Registry.register(Registry.RECIPE_TYPE, PotionMixingRecipe.Type.ID, PotionMixingRecipe.Type.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, PotionMixingRecipeSerializer.ID, PotionMixingRecipeSerializer.INSTANCE);

        NIDWEEN_GROUP.initialize();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

}
