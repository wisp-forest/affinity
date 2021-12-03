package com.glisco.affinity;

import com.glisco.affinity.item.AffinityItemGroup;
import com.glisco.affinity.registries.AffinityBlocks;
import com.glisco.affinity.registries.AffinityItems;
import com.glisco.affinity.registries.AffinityStatusEffects;
import com.glisco.affinity.util.recipe.PotionMixingRecipe;
import com.glisco.affinity.util.recipe.PotionMixingRecipeSerializer;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Affinity implements ModInitializer {

    public static final String MOD_ID = "affinity";
    public static final OwoItemGroup AFFINITY_GROUP = new AffinityItemGroup(id("affinity"));

    @Override
    public void onInitialize() {
        FieldRegistrationHandler.register(AffinityBlocks.class, MOD_ID, false);
        FieldRegistrationHandler.register(AffinityBlocks.Entities.class, MOD_ID, false);

        FieldRegistrationHandler.register(AffinityItems.class, MOD_ID, false);

        AffinityStatusEffects.register();

        Registry.register(Registry.RECIPE_TYPE, PotionMixingRecipe.Type.ID, PotionMixingRecipe.Type.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, PotionMixingRecipeSerializer.ID, PotionMixingRecipeSerializer.INSTANCE);

        AFFINITY_GROUP.initialize();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

}
