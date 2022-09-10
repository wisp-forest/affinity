package io.wispforest.affinity.misc.recipe;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.Ingrediente;
import io.wispforest.affinity.misc.potion.PotionMixture;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public class PotionMixingRecipe implements Recipe<Inventory> {

    private final List<Ingrediente<Boolean>> itemInputs;
    private final List<StatusEffect> effectInputs;
    private final Potion output;

    private final Identifier id;

    public PotionMixingRecipe(Identifier id, List<Ingrediente<Boolean>> itemInputs, List<StatusEffect> effectInputs, Potion output) {
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

            final var effectInputs = Stream.concat(inputMixture.effects().stream(), inputMixture.basePotion().getEffects().stream()).map(StatusEffectInstance::getEffectType).toList();
            final var itemInputs = new ConcurrentLinkedQueue<>(inputStacks.stream().filter(stack -> !stack.isEmpty()).toList());

            if (effectInputs.size() != recipe.effectInputs.size() || itemInputs.size() != recipe.itemInputs.size()) continue;

            int confirmedItemInputs = 0;

            for (var input : recipe.itemInputs) {
                for (var stack : itemInputs) {
                    if (!input.test(stack)) continue;

                    itemInputs.remove(stack);
                    confirmedItemInputs++;
                    break;
                }
            }

            //Test for awkward potion input if no effects have been declared
            boolean effectsConfirmed = recipe.effectInputs.isEmpty() ? inputMixture.basePotion() == Potions.AWKWARD : effectInputs.containsAll(recipe.effectInputs);

            if (!effectsConfirmed || confirmedItemInputs != recipe.itemInputs.size()) continue;

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

    public Potion potionOutput() {
        return output;
    }

    public PotionMixture craftPotion(List<ItemStack> inputStacks) {
        var extraNbt = new NbtCompound();

        for (var ingredient : itemInputs) {
            if (!ingredient.extraData()) continue;

            for (var stack : inputStacks) {
                if (!ingredient.test(stack)) continue;

                if (stack.hasNbt()) {
                    extraNbt.copyFrom(stack.getNbt());
                }

                break;
            }
        }

        return new PotionMixture(this.potionOutput(), extraNbt.isEmpty() ? null : extraNbt);
    }

    public List<Ingrediente<Boolean>> getItemInputs() {
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
