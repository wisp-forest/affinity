package io.wispforest.affinity.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.mixin.access.ShapedRecipeAccessor;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
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
        public Codec<ShapedRecipe> codec() {
            return ((MapCodec.MapCodecCodec<ShapedRecipe>) super.codec()).codec().<ShapedRecipe>xmap(
                    recipe -> new ShapedAssemblyRecipe(recipe.getGroup(), recipe.getCategory(), ((ShapedRecipeAccessor) recipe).affinity$getRaw(), ((ShapedRecipeAccessor) recipe).affinity$getResult(), recipe.showNotification()),
                    recipe -> new ShapedRecipe(recipe.getGroup(), recipe.getCategory(), ((ShapedRecipeAccessor) recipe).affinity$getRaw(), ((ShapedRecipeAccessor) recipe).affinity$getResult(), recipe.showNotification())
            ).codec();
        }

        @Override
        public ShapedAssemblyRecipe read(PacketByteBuf packetByteBuf) {
            final var recipe = super.read(packetByteBuf);
            return new ShapedAssemblyRecipe(recipe.getGroup(), recipe.getCategory(), ((ShapedRecipeAccessor) recipe).affinity$getRaw(), ((ShapedRecipeAccessor) recipe).affinity$getResult(), recipe.showNotification());
        }
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }
}
