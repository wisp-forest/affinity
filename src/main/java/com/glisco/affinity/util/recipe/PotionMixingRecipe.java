package com.glisco.affinity.util.recipe;

import com.glisco.affinity.Affinity;
import com.glisco.affinity.util.potion.PotionMixture;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PotionMixingRecipe implements Recipe<Inventory> {

    private final List<Ingredient> itemInputs;
    private final List<StatusEffect> effectInputs;
    private final Potion output;

    private final Identifier id;

    public PotionMixingRecipe(Identifier id, List<Ingredient> itemInputs, List<StatusEffect> effectInputs, Potion output) {
        this.id = id;

        this.itemInputs = itemInputs;
        this.effectInputs = effectInputs;
        this.output = output;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    @Deprecated
    public boolean matches(Inventory inventory, World world) {
        return false;
    }

    public static Optional<PotionMixingRecipe> getMatching(RecipeManager manager, PotionMixture inputMixture, List<ItemStack> inputStacks) {

        if (inputMixture.isEmpty()) return Optional.empty();

        for (var recipe : manager.listAllOfType(Type.INSTANCE)) {

            final var effects = inputMixture.getCustomEffects().stream().map(StatusEffectInstance::getEffectType).toList();
            final var mutableItems = new ConcurrentLinkedQueue<>(inputStacks.stream().filter(stack -> !stack.isEmpty()).toList());

            if(effects.size() != recipe.effectInputs.size() || mutableItems.size() != recipe.itemInputs.size()) continue;

            final var confirmedIngredients = new ArrayList<Ingredient>();

            for (var ingredient : recipe.itemInputs) {
                for (var stack : mutableItems) {
                    if (!ingredient.test(stack)) continue;
                    mutableItems.remove(stack);
                    confirmedIngredients.add(ingredient);
                    break;
                }
            }

            //Test for awkward potion input if no effects have been declared
            boolean effectsConfirmed = recipe.effectInputs.isEmpty() ? inputMixture.getBasePotion() == Potions.AWKWARD : effects.containsAll(recipe.effectInputs);

            if (!effectsConfirmed || confirmedIngredients.size() != recipe.itemInputs.size()) continue;

            return Optional.of(recipe);
        }
        return Optional.empty();
    }

    @Override
    @Deprecated
    public ItemStack craft(Inventory inventory) {
        return ItemStack.EMPTY;
    }

    @Override
    @Deprecated
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    @Deprecated
    public ItemStack getOutput() {
        return ItemStack.EMPTY;
    }

    public Potion getPotionOutput() {
        return output;
    }

    public List<Ingredient> getItemInputs() {
        return itemInputs;
    }

    public List<StatusEffect> getEffectInputs() {
        return effectInputs;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PotionMixingRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<PotionMixingRecipe> {
        private Type() {}

        public static final Type INSTANCE = new Type();
        public static final Identifier ID = Affinity.id("potion_mixing");
    }

}
