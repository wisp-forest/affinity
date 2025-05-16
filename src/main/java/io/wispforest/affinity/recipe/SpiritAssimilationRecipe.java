package io.wispforest.affinity.recipe;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonParseException;
import io.wispforest.affinity.blockentity.impl.SpiritIntegrationApparatusBlockEntity;
import io.wispforest.affinity.misc.util.EndecUtil;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.EndecRecipeSerializer;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.serialization.format.nbt.NbtEndec;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class SpiritAssimilationRecipe extends RitualRecipe<SpiritIntegrationApparatusBlockEntity.SpiritAssimilationRecipeInput> {

    private static final StructEndec<SpiritAssimilationRecipe> ENDEC = StructEndecBuilder.of(
        EndecUtil.INGREDIENT_ENDEC.listOf().fieldOf("core_inputs", recipe -> recipe.coreInputs),
        EndecUtil.INGREDIENT_ENDEC.listOf().fieldOf("socle_inputs", recipe -> recipe.socleInputs),
        Endec.INT.optionalFieldOf("transfer_components_index", recipe -> recipe.transferComponentsIndex, -1),
        EntityData.ENDEC.fieldOf("entity", recipe -> new EntityData(recipe.entityType, recipe.entityNbt)),
        EndecUtil.RECIPE_RESULT_ENDEC.fieldOf("output", recipe -> recipe.output),
        Endec.INT.optionalFieldOf("duration", recipe -> recipe.duration, 100),
        Endec.INT.validate(duration -> {
            if (duration % 4 != 0) throw new JsonParseException("Spirit assimilation flux cost must be divisible by 4");
        }).optionalFieldOf("flux_cost_per_tick", recipe -> recipe.fluxCostPerTick, 0),
        (coreInputs, socleInputs, transferNbtIndex, entityData, result, duration, fluxCost) -> {
            return new SpiritAssimilationRecipe(coreInputs, socleInputs, transferNbtIndex, entityData.type, entityData.nbt, duration, fluxCost, result);
        }
    );

    public final List<Ingredient> coreInputs;
    public final int transferComponentsIndex;
    public final EntityType<?> entityType;
    @Nullable private final NbtCompound entityNbt;

    private final ItemStack output;

    protected SpiritAssimilationRecipe(List<Ingredient> coreInputs,
                                       List<Ingredient> inputs,
                                       int transferComponentsIndex,
                                       EntityType<?> entityType,
                                       @Nullable NbtCompound entityNbt,
                                       int duration,
                                       int fluxCostPerTick,
                                       ItemStack output) {
        super(inputs, duration, fluxCostPerTick);
        this.coreInputs = ImmutableList.copyOf(coreInputs);
        this.transferComponentsIndex = transferComponentsIndex;
        this.entityType = entityType;
        this.entityNbt = entityNbt;
        this.output = output;
    }

    @Override
    public boolean matches(SpiritIntegrationApparatusBlockEntity.SpiritAssimilationRecipeInput inventory, World world) {
        return this.doShapelessMatch(this.coreInputs, Arrays.asList(inventory.coreInputs()))
            && this.doShapelessMatch(this.socleInputs, inventory.delegate())
            && this.entityType == inventory.sacrifice().getType()
            && (this.entityNbt == null || NbtHelper.matches(this.entityNbt, inventory.sacrifice().writeNbt(new NbtCompound()), true));
    }

    @Override
    public ItemStack craft(SpiritIntegrationApparatusBlockEntity.SpiritAssimilationRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        var result = this.output.copy();

        if (this.transferComponentsIndex != -1) {
            var transferNbtIngredient = this.coreInputs.get(this.transferComponentsIndex);

            for (var stack : input.coreInputs()) {
                if (!transferNbtIngredient.test(stack)) continue;

                result.applyUnvalidatedChanges(stack.getComponentChanges());
                break;
            }
        }

        return result;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup lookup) {
        return this.output.copy();
    }

    public @Nullable NbtCompound entityNbt() {
        return this.entityNbt != null
            ? this.entityNbt.copy()
            : null;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AffinityRecipeTypes.Serializers.SPIRIT_ASSIMILATION;
    }

    @Override
    public RecipeType<?> getType() {
        return AffinityRecipeTypes.SPIRIT_ASSIMILATION;
    }

    private record EntityData(EntityType<?> type, @Nullable NbtCompound nbt) {
        public static final Endec<EntityData> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.ofRegistry(Registries.ENTITY_TYPE).fieldOf("id", EntityData::type),
            NbtEndec.COMPOUND.optionalFieldOf("data", EntityData::nbt, (NbtCompound) null),
            EntityData::new
        );
    }

    public static class Serializer extends EndecRecipeSerializer<SpiritAssimilationRecipe> {
        public Serializer() {
            super(SpiritAssimilationRecipe.ENDEC);
        }
    }
}
