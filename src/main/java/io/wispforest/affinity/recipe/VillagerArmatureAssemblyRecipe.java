package io.wispforest.affinity.recipe;

import com.google.gson.JsonObject;
import io.wispforest.affinity.blockentity.impl.VillagerArmatureBlockEntity;
import io.wispforest.affinity.mixin.access.ShapedRecipeAccessor;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;

public class VillagerArmatureAssemblyRecipe extends ShapedAssemblyRecipe {

    public VillagerArmatureAssemblyRecipe(Identifier id, String group, CraftingRecipeCategory category, int width, int height, DefaultedList<Ingredient> input, ItemStack output) {
        super(id, group, category, width, height, input, output);
    }

    @Override
    public boolean matches(RecipeInputInventory input, World world) {
        for (var stack : input.getInputStacks()) {
            if (!stack.isOf(AffinityItems.VILLAGER_ARMS)) continue;

            var data = stack.get(VillagerArmatureBlockEntity.VILLAGER_DATA);
            if (data != null && data.getProfession() == VillagerProfession.NITWIT) {
                return false;
            }
        }

        return super.matches(input, world);
    }


    @Override
    public ItemStack craft(RecipeInputInventory input, DynamicRegistryManager dynamicRegistryManager) {
        var villagerData = input.getInputStacks().stream()
            .filter(stack -> stack.isOf(AffinityItems.VILLAGER_ARMS) && stack.has(VillagerArmatureBlockEntity.VILLAGER_DATA))
            .map(stack -> stack.get(VillagerArmatureBlockEntity.VILLAGER_DATA))
            .findAny();

        var result = super.craft(input, dynamicRegistryManager);
        villagerData.ifPresent(data -> result.put(VillagerArmatureBlockEntity.VILLAGER_DATA, data));
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AffinityRecipeTypes.Serializers.VILLAGER_ARMATURE_ASSEMBLY;
    }

    public static class Serializer extends ShapedRecipe.Serializer {

        @Override
        public ShapedRecipe read(Identifier identifier, JsonObject jsonObject) {
            var recipe = super.read(identifier, jsonObject);
            return new VillagerArmatureAssemblyRecipe(
                recipe.getId(),
                recipe.getGroup(),
                recipe.getCategory(),
                recipe.getWidth(),
                recipe.getHeight(),
                recipe.getIngredients(),
                ((ShapedRecipeAccessor) recipe).affinity$getOutput()
            );
        }

        @Override
        public ShapedRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            var recipe = super.read(identifier, packetByteBuf);
            return new VillagerArmatureAssemblyRecipe(
                recipe.getId(),
                recipe.getGroup(),
                recipe.getCategory(),
                recipe.getWidth(),
                recipe.getHeight(),
                recipe.getIngredients(),
                ((ShapedRecipeAccessor) recipe).affinity$getOutput()
            );
        }
    }
}
