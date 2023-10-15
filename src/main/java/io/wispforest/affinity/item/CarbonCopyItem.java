package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import io.wispforest.owo.nbt.NbtKey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CarbonCopyItem extends Item {

    public static final NbtKey<Identifier> RECIPE_KEY = new NbtKey<>("Recipe", NbtKey.Type.IDENTIFIER);
    public static final NbtKey<ItemStack> RESULT_KEY = new NbtKey<>("Result", NbtKey.Type.ITEM_STACK);

    public CarbonCopyItem() {
        super(new OwoItemSettings().maxCount(1));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Optional<net.minecraft.client.item.TooltipData> getTooltipData(ItemStack stack) {
        var recipeId = stack.getOr(RECIPE_KEY, null);
        var result = stack.getOr(RESULT_KEY, null);
        if (recipeId == null || result == null) return Optional.empty();

        var recipe = MinecraftClient.getInstance().world.getRecipeManager().get(recipeId);
        if (recipe.isEmpty() || !(recipe.get().value() instanceof CraftingRecipe craftingRecipe)) {
            return Optional.empty();
        }

        return Optional.of(new TooltipData(craftingRecipe, result));
    }

    @Override
    public Text getName() {
        return Text.translatable(this.getTranslationKey() + ".empty");
    }

    @Override
    public Text getName(ItemStack stack) {
        var resultStack = stack.getOr(RESULT_KEY, null);
        return resultStack == null
                ? this.getName()
                : Text.translatable(this.getTranslationKey(), resultStack.getCount(), resultStack.getName());
    }

    public static ItemStack create(RecipeEntry<CraftingRecipe> recipe, ItemStack result) {
        var stack = AffinityItems.CARBON_COPY.getDefaultStack();

        stack.put(RECIPE_KEY, recipe.id());
        stack.put(RESULT_KEY, result);

        return stack;
    }

    @SuppressWarnings("unchecked")
    public static @Nullable RecipeEntry<CraftingRecipe> getRecipe(ItemStack stack, World world) {
        if (!stack.has(RECIPE_KEY)) return null;

        var entry = world.getRecipeManager().get(stack.get(RECIPE_KEY)).orElse(null);
        if (entry == null || !(entry.value() instanceof CraftingRecipe)) {
            return null;
        }

        return (RecipeEntry<CraftingRecipe>) entry;
    }

    public record TooltipData(CraftingRecipe recipe,
                              ItemStack result) implements net.minecraft.client.item.TooltipData {}
}
