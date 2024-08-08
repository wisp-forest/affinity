package io.wispforest.affinity.recipe;

import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.mixin.access.ShapedRecipeAccessor;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;

public class ShapedAssemblyRecipe extends ShapedRecipe {

    public ShapedAssemblyRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe raw, ItemStack result, boolean showNotification) {
        super(group, category, raw, result, showNotification);
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
        public MapCodec<ShapedRecipe> codec() {
            return super.codec().xmap(
                    recipe -> new ShapedAssemblyRecipe(recipe.getGroup(), recipe.getCategory(), ((ShapedRecipeAccessor) recipe).affinity$getRaw(), ((ShapedRecipeAccessor) recipe).affinity$getResult(), recipe.showNotification()),
                    recipe -> new ShapedRecipe(recipe.getGroup(), recipe.getCategory(), ((ShapedRecipeAccessor) recipe).affinity$getRaw(), ((ShapedRecipeAccessor) recipe).affinity$getResult(), recipe.showNotification())
            );
        }

        @Override
        public PacketCodec<RegistryByteBuf, ShapedRecipe> packetCodec() {
            return super.packetCodec().xmap(
                recipe -> new ShapedAssemblyRecipe(recipe.getGroup(), recipe.getCategory(), ((ShapedRecipeAccessor) recipe).affinity$getRaw(), ((ShapedRecipeAccessor) recipe).affinity$getResult(), recipe.showNotification()),
                recipe -> new ShapedRecipe(recipe.getGroup(), recipe.getCategory(), ((ShapedRecipeAccessor) recipe).affinity$getRaw(), ((ShapedRecipeAccessor) recipe).affinity$getResult(), recipe.showNotification())
            );
        }
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }
}
