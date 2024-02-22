package io.wispforest.affinity.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public class PotionMixingRecipe implements Recipe<Inventory> {

    public static final Codec<PotionMixingRecipe> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Registries.STATUS_EFFECT.getCodec().listOf().fieldOf("effect_inputs").forGetter(recipe -> recipe.effectInputs),
                    Ingredient.DISALLOW_EMPTY_CODEC.listOf().fieldOf("item_inputs").forGetter(recipe -> recipe.itemInputs),
                    Codec.INT.optionalFieldOf("copy_nbt_index", -1).forGetter(recipe -> recipe.copyNbtIndex),
                    Registries.POTION.getCodec().fieldOf("output").forGetter(recipe -> recipe.output),
                    Codec.BOOL.optionalFieldOf("strong", false).forGetter(recipe -> recipe.strong)
            )
            .apply(instance, PotionMixingRecipe::new));

    public final List<StatusEffect> effectInputs;
    public final List<Ingredient> itemInputs;
    public final int copyNbtIndex;
    private final Potion output;
    public final boolean strong;

    public PotionMixingRecipe(List<StatusEffect> effectInputs, List<Ingredient> itemInputs, int copyNbtIndex, Potion output, boolean strong) {
        this.effectInputs = effectInputs;
        this.itemInputs = itemInputs;
        this.copyNbtIndex = copyNbtIndex;
        this.output = output;
        this.strong = strong;
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

        for (var recipeEntry : manager.listAllOfType(AffinityRecipeTypes.POTION_MIXING)) {
            var recipe = recipeEntry.value();

            var effectInputs = Stream.concat(inputMixture.effects().stream(), inputMixture.basePotion().getEffects().stream()).map(StatusEffectInstance::getEffectType).distinct().toList();
            var itemInputs = new ConcurrentLinkedQueue<>(inputStacks.stream().filter(stack -> !stack.isEmpty()).toList());

            if (effectInputs.size() != recipe.effectInputs.size() || itemInputs.size() != recipe.itemInputs.size()) {
                continue;
            }

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
    public ItemStack craft(Inventory inventory, DynamicRegistryManager drm) {
        return ItemStack.EMPTY;
    }

    @Override
    @Deprecated
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    @Deprecated
    public ItemStack getResult(DynamicRegistryManager drm) {
        return ItemStack.EMPTY;
    }

    public Potion potionOutput() {
        return output;
    }

    public PotionMixture craftPotion(List<ItemStack> inputStacks) {
        var extraNbt = new NbtCompound();

        if (this.copyNbtIndex != -1) {
            var copyNbtIngredient = this.itemInputs.get(this.copyNbtIndex);
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
}
