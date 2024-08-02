package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentType;
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

    public static final ComponentType<RecipeComponent> RECIPE = Affinity.component(
            "carbon_copy_recipe",
            RecipeComponent.ENDEC
    );

    public CarbonCopyItem() {
        super(new OwoItemSettings().maxCount(1));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Optional<net.minecraft.item.tooltip.TooltipData> getTooltipData(ItemStack stack) {
        if (!stack.contains(RECIPE)) return Optional.empty();
        var recipeComponent = stack.get(RECIPE);

        var recipe = MinecraftClient.getInstance().world.getRecipeManager().get(recipeComponent.recipeId);
        if (recipe.isEmpty() || !(recipe.get().value() instanceof CraftingRecipe craftingRecipe)) {
            return Optional.empty();
        }

        return Optional.of(new TooltipData(craftingRecipe, recipeComponent.result));
    }

    @Override
    public Text getName() {
        return Text.translatable(this.getTranslationKey() + ".empty");
    }

    @Override
    public Text getName(ItemStack stack) {
        var component = stack.get(RECIPE);
        return component == null
                ? this.getName()
                : Text.translatable(this.getTranslationKey(), component.result.getCount(), component.result.getName());
    }

    public static ItemStack create(RecipeEntry<CraftingRecipe> recipe, ItemStack result) {
        var stack = AffinityItems.CARBON_COPY.getDefaultStack();
        stack.set(RECIPE, new RecipeComponent(recipe.id(), result));
        return stack;
    }

    @SuppressWarnings("unchecked")
    public static @Nullable RecipeEntry<CraftingRecipe> getRecipe(ItemStack stack, World world) {
        if (!stack.contains(RECIPE)) return null;

        var entry = world.getRecipeManager().get(stack.get(RECIPE).recipeId).orElse(null);
        if (entry == null || !(entry.value() instanceof CraftingRecipe)) {
            return null;
        }

        return (RecipeEntry<CraftingRecipe>) entry;
    }

    public record RecipeComponent(Identifier recipeId, ItemStack result) {
        public static final Endec<RecipeComponent> ENDEC = StructEndecBuilder.of(
                MinecraftEndecs.IDENTIFIER.fieldOf("recipe_id", RecipeComponent::recipeId),
                MinecraftEndecs.ITEM_STACK.fieldOf("result", RecipeComponent::result),
                RecipeComponent::new
        );
    }

    public record TooltipData(CraftingRecipe recipe, ItemStack result) implements net.minecraft.item.tooltip.TooltipData {}
}
