package io.wispforest.affinity.recipe;

import io.wispforest.affinity.misc.potion.ExtraPotionData;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.misc.util.EndecUtil;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.EndecRecipeSerializer;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public class PotionMixingRecipe implements Recipe<PotionMixingRecipe.Input> {

    public static final StructEndec<PotionMixingRecipe> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.ofRegistry(Registries.STATUS_EFFECT).listOf().fieldOf("effect_inputs", recipe -> recipe.effectInputs),
            EndecUtil.INGREDIENT_ENDEC.listOf().fieldOf("item_inputs", recipe -> recipe.itemInputs),
            Endec.INT.optionalFieldOf("copy_nbt_index", recipe -> recipe.copyComponentsIndex, -1),
            MinecraftEndecs.ofRegistry(Registries.POTION).fieldOf("output", recipe -> recipe.output),
            Endec.BOOLEAN.optionalFieldOf("strong", recipe -> recipe.strong, false),
            PotionMixingRecipe::new
    );

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
        var extraData = new ComponentMapImpl(ComponentMap.EMPTY);

        if (this.copyComponentsIndex != -1) {
            var copyNbtIngredient = this.itemInputs.get(this.copyComponentsIndex);
            for (var stack : inputStacks) {
                if (!copyNbtIngredient.test(stack)) continue;

                ExtraPotionData.copyExtraData(stack, extraData);

                break;
            }
        }

        return new PotionMixture(this.potionOutput(), extraData);
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

    public static final class Serializer extends EndecRecipeSerializer<PotionMixingRecipe> {
        public Serializer() {
            super(PotionMixingRecipe.ENDEC);
        }
    }
}
