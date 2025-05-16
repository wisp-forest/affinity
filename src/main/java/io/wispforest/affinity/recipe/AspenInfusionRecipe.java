package io.wispforest.affinity.recipe;

import io.wispforest.affinity.blockentity.impl.AspRiteCoreBlockEntity;
import io.wispforest.affinity.misc.util.EndecUtil;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.EndecRecipeSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.List;

public class AspenInfusionRecipe extends RitualRecipe<AspRiteCoreBlockEntity.AspenInfusionRecipeInput> {

    private static final StructEndec<AspenInfusionRecipe> ENDEC = StructEndecBuilder.of(
            EndecUtil.INGREDIENT_ENDEC.fieldOf("primary_input", recipe -> recipe.primaryInput),
            EndecUtil.INGREDIENT_ENDEC.listOf().fieldOf("inputs", recipe -> recipe.socleInputs),
            EndecUtil.RECIPE_RESULT_ENDEC.fieldOf("output", recipe -> recipe.output),
            Endec.BOOLEAN.optionalFieldOf("transfer_components", recipe -> recipe.transferComponents, false),
            Endec.INT.optionalFieldOf("duration", recipe -> recipe.duration, 100),
            Endec.INT.optionalFieldOf("flux_cost_per_tick", recipe -> recipe.fluxCostPerTick, 0),
            AspenInfusionRecipe::new
    );

    public final Ingredient primaryInput;
    private final ItemStack output;
    private final boolean transferComponents;

    public AspenInfusionRecipe(Ingredient primaryInput, List<Ingredient> inputs, ItemStack output, boolean transferComponents, int duration, int fluxCostPerTick) {
        super(inputs, duration, fluxCostPerTick);
        this.primaryInput = primaryInput;
        this.output = output;
        this.transferComponents = transferComponents;
    }

    @Override
    public boolean matches(AspRiteCoreBlockEntity.AspenInfusionRecipeInput inventory, World world) {
        return this.primaryInput.test(inventory.primaryInput()) && this.doShapelessMatch(this.socleInputs, inventory.delegate());
    }

    @Override
    public ItemStack craft(AspRiteCoreBlockEntity.AspenInfusionRecipeInput inventory, RegistryWrapper.WrapperLookup registries) {
        var result = this.output.copy();

        if (this.transferComponents) {
            result.applyUnvalidatedChanges(inventory.primaryInput().getComponentChanges());
        }

        return result;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registries) {
        return this.output.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AffinityRecipeTypes.Serializers.ASPEN_INFUSION;
    }

    @Override
    public RecipeType<?> getType() {
        return AffinityRecipeTypes.ASPEN_INFUSION;
    }

    public static final class Serializer extends EndecRecipeSerializer<AspenInfusionRecipe> {
        public Serializer() {
            super(AspenInfusionRecipe.ENDEC);
        }
    }
}
