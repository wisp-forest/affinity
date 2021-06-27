package com.glisco.nidween.util.recipe;

import com.glisco.nidween.Nidween;
import com.glisco.nidween.util.potion.PotionMixture;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
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

    public static Optional<PotionMixingRecipe> getMatching(RecipeManager manager, PotionMixture mixture, List<ItemStack> stacks) {

        if(mixture.isEmpty()) return Optional.empty();

        //TODO exact matching, allow for longer and stronger
        for (var recipe : manager.listAllOfType(Type.INSTANCE)) {
            final var effects = mixture.getCustomEffects().stream().map(StatusEffectInstance::getEffectType).toList();

            final var mutableItems = new ConcurrentLinkedQueue<>(stacks);
            final var confirmedIngredients = new ArrayList<Ingredient>();

            for (var ingredient : recipe.itemInputs){
                for (var stack : mutableItems){
                    if(!ingredient.test(stack)) continue;
                    mutableItems.remove(stack);
                    confirmedIngredients.add(ingredient);
                    break;
                }
            }

            if(!effects.containsAll(recipe.effectInputs) || confirmedIngredients.size() != recipe.itemInputs.size()) continue;

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
        public static final Identifier ID = Nidween.id("potion_mixing");
    }

}
