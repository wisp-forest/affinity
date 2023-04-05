package io.wispforest.affinity.recipe;

import com.google.gson.JsonObject;
import io.wispforest.affinity.mixin.access.ShapelessRecipeAccessor;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class ShapelessAssemblyRecipe extends ShapelessRecipe {

    public ShapelessAssemblyRecipe(Identifier id, String group, CraftingRecipeCategory category, ItemStack output, DefaultedList<Ingredient> input) {
        super(id, group, category, output, input);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AffinityRecipeTypes.Serializers.ASSEMBLY_SHAPELESS;
    }

    @Override
    public RecipeType<?> getType() {
        return AffinityRecipeTypes.ASSEMBLY;
    }

    public static class Serializer extends ShapelessRecipe.Serializer {
        @Override
        public ShapelessRecipe read(Identifier identifier, JsonObject jsonObject) {
            final var recipe = super.read(identifier, jsonObject);
            return new ShapelessAssemblyRecipe(recipe.getId(), recipe.getGroup(), recipe.getCategory(), ((ShapelessRecipeAccessor) recipe).affinity$getOutput(), recipe.getIngredients());
        }

        @Override
        public ShapelessRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            var recipe = super.read(identifier, packetByteBuf);
            return new ShapelessAssemblyRecipe(recipe.getId(), recipe.getGroup(), recipe.getCategory(), ((ShapelessRecipeAccessor) recipe).affinity$getOutput(), recipe.getIngredients());
        }
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }
}
