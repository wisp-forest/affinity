package io.wispforest.affinity.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.mixin.access.ShapelessRecipeAccessor;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.collection.DefaultedList;

public class ShapelessAssemblyRecipe extends ShapelessRecipe {

    public ShapelessAssemblyRecipe(String group, CraftingRecipeCategory category, ItemStack output, DefaultedList<Ingredient> input) {
        super(group, category, output, input);
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
        public Codec<ShapelessRecipe> codec() {
            return ((MapCodec.MapCodecCodec<ShapelessRecipe>) super.codec()).codec().<ShapelessRecipe>xmap(
                    recipe -> new ShapelessAssemblyRecipe(recipe.getGroup(), recipe.getCategory(), ((ShapelessRecipeAccessor) recipe).affinity$getResult(), recipe.getIngredients()),
                    recipe -> new ShapelessRecipe(recipe.getGroup(), recipe.getCategory(), ((ShapelessRecipeAccessor) recipe).affinity$getResult(), recipe.getIngredients())
            ).codec();
        }

        @Override
        public ShapelessRecipe read(PacketByteBuf packetByteBuf) {
            var recipe = super.read(packetByteBuf);
            return new ShapelessAssemblyRecipe(recipe.getGroup(), recipe.getCategory(), ((ShapelessRecipeAccessor) recipe).affinity$getResult(), recipe.getIngredients());
        }
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }
}
