package io.wispforest.affinity.recipe;

import io.wispforest.affinity.blockentity.template.RitualCoreBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;

public abstract class RitualRecipe<I extends RitualCoreBlockEntity.SocleInventory> implements Recipe<I> {

    protected final Identifier id;
    protected final List<Ingredient> inputs;
    protected final int duration;

    protected RitualRecipe(Identifier id, List<Ingredient> inputs, int duration) {
        this.id = id;
        this.inputs = inputs;
        this.duration = duration;
    }

    protected boolean soclesMatchInputs(I inventory) {
        final var matcher = new RecipeMatcher();
        int nonEmptyStacks = 0;

        for (int j = 0; j < inventory.size(); ++j) {
            ItemStack itemStack = inventory.getStack(j);
            if (!itemStack.isEmpty()) {
                ++nonEmptyStacks;
                matcher.addInput(itemStack, 1);
            }
        }

        return nonEmptyStacks == this.inputs.size() && matcher.match(this, null);
    }

    protected boolean runRecipeMatcher(List<Ingredient> expected, Collection<ItemStack> stacks) {
        final var matcher = new RecipeMatcher();
        int nonEmptyStacks = 0;

        for (var stack : stacks) {
            if (!stack.isEmpty()) {
                ++nonEmptyStacks;
                matcher.addInput(stack, 1);
            }
        }

        return nonEmptyStacks == expected.size() &&
                matcher.match(new MatchingRecipe(DefaultedList.copyOf(Ingredient.EMPTY, expected.toArray(Ingredient[]::new))), null);
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return DefaultedList.copyOf(Ingredient.EMPTY, this.inputs.toArray(Ingredient[]::new));
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    private record MatchingRecipe(DefaultedList<Ingredient> ingredients) implements Recipe<Inventory> {

        @Override
        public DefaultedList<Ingredient> getIngredients() {
            return this.ingredients;
        }

        @Override
        public boolean matches(Inventory inventory, World world) {
            return false;
        }

        @Override
        public ItemStack craft(Inventory inventory) {
            return null;
        }

        @Override
        public boolean fits(int width, int height) {
            return false;
        }

        @Override
        public ItemStack getOutput() {
            return null;
        }

        @Override
        public Identifier getId() {
            return null;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return null;
        }

        @Override
        public RecipeType<?> getType() {
            return null;
        }
    }
}
