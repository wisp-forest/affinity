package io.wispforest.affinity.recipe;

import com.mojang.serialization.Codec;
import io.wispforest.affinity.mixin.access.ShapedRecipeAccessor;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.collection.DefaultedList;

public class ShapedAssemblyRecipe extends ShapedRecipe {

    public ShapedAssemblyRecipe(String group, CraftingRecipeCategory category, int width, int height, DefaultedList<Ingredient> input, ItemStack output) {
        super(group, category, width, height, input, output);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AffinityRecipeTypes.Serializers.ASSEMBLY_SHAPED;
    }

    @Override
    public RecipeType<?> getType() {
        return AffinityRecipeTypes.ASSEMBLY;
    }

    public static class Serializer extends ShapedRecipe.Serializer {

        @Override
        public Codec<ShapedRecipe> codec() {
            return super.codec().xmap(
                    recipe -> new ShapedAssemblyRecipe(recipe.getGroup(), recipe.getCategory(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), ((ShapedRecipeAccessor) recipe).affinity$getResult()),
                    recipe -> new ShapedRecipe(recipe.getGroup(), recipe.getCategory(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), ((ShapedRecipeAccessor) recipe).affinity$getResult())
            );
        }

        @Override
        public ShapedAssemblyRecipe read(PacketByteBuf packetByteBuf) {
            final var recipe = super.read(packetByteBuf);
            return new ShapedAssemblyRecipe(recipe.getGroup(), recipe.getCategory(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), ((ShapedRecipeAccessor) recipe).affinity$getResult());
        }
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }
}
