package io.wispforest.affinity.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public class PotionMixingRecipe implements Recipe<PotionMixingRecipe.Input> {

    public static final Codec<PotionMixingRecipe> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Registries.STATUS_EFFECT.getCodec().listOf().fieldOf("effect_inputs").forGetter(recipe -> recipe.effectInputs),
                    Ingredient.DISALLOW_EMPTY_CODEC.listOf().fieldOf("item_inputs").forGetter(recipe -> recipe.itemInputs),
                    Codec.INT.optionalFieldOf("copy_nbt_index", -1).forGetter(recipe -> recipe.copyComponentsIndex),
                    Registries.POTION.getCodec().fieldOf("output").forGetter(recipe -> recipe.output),
                    Codec.BOOL.optionalFieldOf("strong", false).forGetter(recipe -> recipe.strong)
            )
            .apply(instance, PotionMixingRecipe::new));

    public final List<StatusEffect> effectInputs;
    public final List<Ingredient> itemInputs;
    public final int copyComponentsIndex;
    private final Potion output;
    public final boolean strong;

    public PotionMixingRecipe(List<StatusEffect> effectInputs, List<Ingredient> itemInputs, int copyComponentsIndex, Potion output, boolean strong) {
        this.effectInputs = effectInputs;
        this.itemInputs = itemInputs;
        this.copyComponentsIndex = copyComponentsIndex;
        this.output = output;
        this.strong = strong;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    @Deprecated
    public boolean matches(Input input, World world) {
        if (input.mixture.isEmpty()) return false;

        var effectInputs = Stream.concat(input.mixture.effects().stream(), input.mixture.basePotion().getEffects().stream()).map(StatusEffectInstance::getEffectType).distinct().toList();
        var itemInputs = new ConcurrentLinkedQueue<>(input.items.stream().filter(stack -> !stack.isEmpty()).toList());

        if (effectInputs.size() != this.effectInputs.size() || itemInputs.size() != this.itemInputs.size()) {
            return false;
        }

        int confirmedItemInputs = 0;

        for (var inputStack : this.itemInputs) {
            for (var stack : itemInputs) {
                if (!inputStack.test(stack)) continue;

                itemInputs.remove(stack);
                confirmedItemInputs++;
                break;
            }
        }

        //Test for awkward potion input if no effects have been declared
        boolean effectsConfirmed = this.effectInputs.isEmpty() ? input.mixture.basePotion() == Potions.AWKWARD : effectInputs.containsAll(this.effectInputs);

        return effectsConfirmed && confirmedItemInputs == this.itemInputs.size();
    }

    // TODO this might be bad
    @Override
    @Deprecated
    public ItemStack craft(Input input, RegistryWrapper.WrapperLookup registries) {
        return ItemStack.EMPTY;
    }

    @Override
    @Deprecated
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    @Deprecated
    public ItemStack getResult(RegistryWrapper.WrapperLookup registries) {
        return ItemStack.EMPTY;
    }

    public Potion potionOutput() {
        return output;
    }

    public PotionMixture craftPotion(List<ItemStack> inputStacks) {
        var extraNbt = new NbtCompound();

        if (this.copyComponentsIndex != -1) {
            var copyNbtIngredient = this.itemInputs.get(this.copyComponentsIndex);
            for (var stack : inputStacks) {
                if (!copyNbtIngredient.test(stack)) continue;

                if (stack.hasNbt()) {
                    extraNbt.copyFrom(stack.getNbt());
                }

                break;
            }
        }

        return new PotionMixture(this.potionOutput(), extraNbt.isEmpty() ? null : extraNbt);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AffinityRecipeTypes.Serializers.POTION_MIXING;
    }

    @Override
    public RecipeType<?> getType() {
        return AffinityRecipeTypes.POTION_MIXING;
    }

    public record Input(List<ItemStack> items, PotionMixture mixture) implements RecipeInput {
        @Override
        public ItemStack getStackInSlot(int slot) {
            return this.items.get(slot);
        }

        @Override
        public int getSize() {
            return this.items.size();
        }
    }
}
