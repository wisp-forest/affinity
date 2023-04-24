package io.wispforest.affinity.object;

import io.wispforest.affinity.recipe.ingredient.EnchantedBookIngredient;
import io.wispforest.affinity.recipe.ingredient.PotionIngredient;
import io.wispforest.owo.registration.reflect.SimpleFieldProcessingSubject;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

import java.lang.reflect.Field;

public class AffinityIngredients implements SimpleFieldProcessingSubject<CustomIngredientSerializer<?>> {

    public static final CustomIngredientSerializer<?> ENCHANTMENTS = new EnchantedBookIngredient.Serializer();
    public static final CustomIngredientSerializer<?> POTION = new PotionIngredient.Serializer();

    @Override
    public void processField(CustomIngredientSerializer<?> value, String identifier, Field field) {
        CustomIngredientSerializer.register(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<CustomIngredientSerializer<?>> getTargetFieldType() {
        return (Class<CustomIngredientSerializer<?>>) (Object) CustomIngredientSerializer.class;
    }
}
