package io.wispforest.affinity.recipe;

import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.item.VillagerArmsItem;
import io.wispforest.affinity.mixin.access.ShapedRecipeAccessor;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;

public class VillagerArmatureAssemblyRecipe extends ShapedAssemblyRecipe {

    public VillagerArmatureAssemblyRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe raw, ItemStack result, boolean showNotification) {
        super(group, category, raw, result, showNotification);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        for (var stack : input.getStacks()) {
            if (!stack.isOf(AffinityItems.VILLAGER_ARMS)) continue;

            var data = stack.get(VillagerArmsItem.VILLAGER_DATA);
            if (data != null && data.profession() == VillagerProfession.NITWIT) {
                return false;
            }
        }

        return super.matches(input, world);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup wrapperLookup) {
        var villagerData = input.getStacks().stream()
            .filter(stack -> stack.isOf(AffinityItems.VILLAGER_ARMS) && stack.contains(VillagerArmsItem.VILLAGER_DATA))
            .map(stack -> stack.get(VillagerArmsItem.VILLAGER_DATA))
            .findAny();

        var result = super.craft(input, wrapperLookup);
        villagerData.ifPresent(data -> result.set(VillagerArmsItem.VILLAGER_DATA, data));
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AffinityRecipeTypes.Serializers.VILLAGER_ARMATURE_ASSEMBLY;
    }

    public static class Serializer extends ShapedRecipe.Serializer {

        @Override
        public MapCodec<ShapedRecipe> codec() {
            return super.codec().xmap(
                recipe -> new VillagerArmatureAssemblyRecipe(recipe.getGroup(), recipe.getCategory(), ((ShapedRecipeAccessor) recipe).affinity$getRaw(), ((ShapedRecipeAccessor) recipe).affinity$getResult(), recipe.showNotification()),
                recipe -> new ShapedRecipe(recipe.getGroup(), recipe.getCategory(), ((ShapedRecipeAccessor) recipe).affinity$getRaw(), ((ShapedRecipeAccessor) recipe).affinity$getResult(), recipe.showNotification())
            );
        }

        @Override
        public PacketCodec<RegistryByteBuf, ShapedRecipe> packetCodec() {
            return super.packetCodec().xmap(
                recipe -> new VillagerArmatureAssemblyRecipe(recipe.getGroup(), recipe.getCategory(), ((ShapedRecipeAccessor) recipe).affinity$getRaw(), ((ShapedRecipeAccessor) recipe).affinity$getResult(), recipe.showNotification()),
                recipe -> new ShapedRecipe(recipe.getGroup(), recipe.getCategory(), ((ShapedRecipeAccessor) recipe).affinity$getRaw(), ((ShapedRecipeAccessor) recipe).affinity$getResult(), recipe.showNotification())
            );
        }
    }

}
