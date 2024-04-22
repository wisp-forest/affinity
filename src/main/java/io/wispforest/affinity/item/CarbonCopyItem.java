package io.wispforest.affinity.item;

import io.wispforest.affinity.endec.BuiltInEndecs;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CarbonCopyItem extends Item {

    public static final KeyedEndec<Identifier> RECIPE_KEY = BuiltInEndecs.IDENTIFIER.keyed("Recipe", (Identifier) null);
    public static final KeyedEndec<ItemStack> RESULT_KEY = BuiltInEndecs.ITEM_STACK.keyed("Result", (ItemStack) null);

    public CarbonCopyItem() {
        super(new OwoItemSettings().maxCount(1));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Optional<net.minecraft.client.item.TooltipData> getTooltipData(ItemStack stack) {
        var recipeId = stack.get(RECIPE_KEY);
        var result = stack.get(RESULT_KEY);
        if (recipeId == null || result == null) return Optional.empty();

        var recipe = MinecraftClient.getInstance().world.getRecipeManager().get(recipeId);
        if (recipe.isEmpty() || !(recipe.get() instanceof CraftingRecipe craftingRecipe)) return Optional.empty();

        return Optional.of(new TooltipData(craftingRecipe, result));
    }

    @Override
    public Text getName() {
        return Text.translatable(this.getTranslationKey() + ".empty");
    }

    @Override
    public Text getName(ItemStack stack) {
        var resultStack = stack.get(RESULT_KEY);
        return resultStack == null
                ? this.getName()
                : Text.translatable(this.getTranslationKey(), resultStack.getCount(), resultStack.getName());
    }

    public static ItemStack create(CraftingRecipe recipe, ItemStack result) {
        var stack = AffinityItems.CARBON_COPY.getDefaultStack();

        stack.put(RECIPE_KEY, recipe.getId());
        stack.put(RESULT_KEY, result);

        return stack;
    }

    public static @Nullable CraftingRecipe getRecipe(ItemStack stack, World world) {
        if (!stack.has(RECIPE_KEY)) return null;
        if (!(world.getRecipeManager().get(stack.get(RECIPE_KEY)).orElse(null) instanceof CraftingRecipe recipe))
            return null;

        return recipe;
    }

    public record TooltipData(CraftingRecipe recipe,
                              ItemStack result) implements net.minecraft.client.item.TooltipData {}
}
