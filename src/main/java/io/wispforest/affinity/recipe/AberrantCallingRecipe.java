package io.wispforest.affinity.recipe;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.wispforest.affinity.blockentity.impl.AberrantCallingCoreBlockEntity;
import io.wispforest.affinity.misc.util.JsonUtil;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AberrantCallingRecipe extends RitualRecipe<AberrantCallingCoreBlockEntity.AberrantCallingInventory> {

    public final List<Ingredient> coreInputs;
    public final EntityType<?> entityType;
    @Nullable private final NbtCompound entityNbt;

    private final ItemStack output;

    protected AberrantCallingRecipe(Identifier id,
                                    List<Ingredient> coreInputs,
                                    List<Ingredient> inputs,
                                    EntityType<?> entityType,
                                    @Nullable NbtCompound entityNbt,
                                    int duration,
                                    int fluxCostPerTick,
                                    ItemStack output) {
        super(id, inputs, duration, fluxCostPerTick);
        this.coreInputs = ImmutableList.copyOf(coreInputs);
        this.entityType = entityType;
        this.entityNbt = entityNbt;
        this.output = output;
    }

    @Override
    public boolean matches(AberrantCallingCoreBlockEntity.AberrantCallingInventory inventory, World world) {
        return this.doShapelessMatch(this.coreInputs, Arrays.asList(inventory.coreInputs()))
                && this.doShapelessMatch(this.socleInputs, inventory.delegate())
                && this.entityType == inventory.sacrifice().getType()
                && (this.entityNbt == null || NbtHelper.matches(this.entityNbt, inventory.sacrifice().writeNbt(new NbtCompound()), true));
    }

    @Override
    public ItemStack craft(AberrantCallingCoreBlockEntity.AberrantCallingInventory inventory) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getOutput() {
        return this.output.copy();
    }

    public @Nullable NbtCompound entityNbt() {
        return this.entityNbt != null
                ? this.entityNbt.copy()
                : null;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return AffinityRecipeTypes.ABERRANT_CALLING;
    }

    public static final class Serializer implements RecipeSerializer<AberrantCallingRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {}

        @Override
        public AberrantCallingRecipe read(Identifier id, JsonObject json) {
            final var entityObject = JsonHelper.getObject(json, "entity");

            int fluxCostPerTick = JsonHelper.getInt(json, "flux_cost_per_tick", 0);
            if (fluxCostPerTick % 4 != 0) {
                throw new JsonParseException("Aberrant calling flux cost must be divisible by 4");
            }

            return new AberrantCallingRecipe(id,
                    JsonUtil.readIngredientList(json, "core_inputs"),
                    JsonUtil.readIngredientList(json, "socle_inputs"),
                    JsonUtil.readFromRegistry(entityObject, "id", Registries.ENTITY_TYPE),
                    entityObject.has("data") ? JsonUtil.readNbt(entityObject, "data") : null,
                    JsonHelper.getInt(json, "duration", 100),
                    fluxCostPerTick,
                    JsonUtil.readChadStack(json, "output")
            );
        }

        @Override
        public AberrantCallingRecipe read(Identifier id, PacketByteBuf buf) {
            return new AberrantCallingRecipe(
                    id,
                    buf.readCollection(ArrayList::new, Ingredient::fromPacket),
                    buf.readCollection(ArrayList::new, Ingredient::fromPacket),
                    Registries.ENTITY_TYPE.get(buf.readVarInt()),
                    buf.readOptional(PacketByteBuf::readNbt).orElse(null),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readItemStack()
            );
        }

        @Override
        public void write(PacketByteBuf buf, AberrantCallingRecipe recipe) {
            buf.writeCollection(recipe.coreInputs, (packetByteBuf, ingredient) -> ingredient.write(packetByteBuf));
            buf.writeCollection(recipe.socleInputs, (packetByteBuf, ingredient) -> ingredient.write(packetByteBuf));

            buf.writeVarInt(Registries.ENTITY_TYPE.getRawId(recipe.entityType));
            buf.writeOptional(Optional.ofNullable(recipe.entityNbt), PacketByteBuf::writeNbt);

            buf.writeVarInt(recipe.duration);
            buf.writeVarInt(recipe.fluxCostPerTick);

            buf.writeItemStack(recipe.output);
        }
    }
}
