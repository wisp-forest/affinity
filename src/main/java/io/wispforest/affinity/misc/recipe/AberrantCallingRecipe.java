package io.wispforest.affinity.misc.recipe;

import com.google.gson.JsonObject;
import io.wispforest.affinity.blockentity.impl.AberrantCallingCoreBlockEntity;
import io.wispforest.affinity.misc.util.JsonUtil;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AberrantCallingRecipe extends RitualRecipe<AberrantCallingCoreBlockEntity.AberrantCallingInventory> {

    private final List<Ingredient> coreInputs;
    private final ItemStack output;

    private final EntityType<?> entityType;
    @Nullable private final NbtCompound entityNbt;

    protected AberrantCallingRecipe(Identifier id,
                                    List<Ingredient> coreInputs,
                                    List<Ingredient> inputs,
                                    EntityType<?> entityType,
                                    @Nullable NbtCompound entityNbt,
                                    ItemStack output,
                                    int duration) {
        super(id, inputs, duration);
        this.coreInputs = coreInputs;
        this.entityType = entityType;
        this.entityNbt = entityNbt;
        this.output = output;
    }

    @Override
    public boolean matches(AberrantCallingCoreBlockEntity.AberrantCallingInventory inventory, World world) {
        return this.runRecipeMatcher(this.coreInputs, Arrays.asList(inventory.coreInputs()))
                && this.soclesMatchInputs(inventory);
    }

    @Override
    public ItemStack craft(AberrantCallingCoreBlockEntity.AberrantCallingInventory inventory) {
        return this.output.copy();
    }

    @Override
    public ItemStack getOutput() {
        return this.output.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return AffinityRecipeTypes.ABERRANT_CALLING;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public NbtCompound getEntityNbt() {
        return entityNbt;
    }

    public static final class Serializer implements RecipeSerializer<AberrantCallingRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {}

        @Override
        public AberrantCallingRecipe read(Identifier id, JsonObject json) {
            final var output = JsonUtil.readChadStack(json, "output");
            final var coreInputs = JsonUtil.readIngredientList(json, "core_inputs");
            final var socleInputs = JsonUtil.readIngredientList(json, "socle_inputs");

            final int duration = JsonHelper.getInt(json, "duration", 100);

            final var entityObject = JsonHelper.getObject(json, "entity");
            final var entityType = JsonUtil.readFromRegistry(entityObject, "id", Registry.ENTITY_TYPE);
            final var entityNbt = entityObject.has("data") ? JsonUtil.readNbt(entityObject, "data") : null;

            return new AberrantCallingRecipe(id, coreInputs, socleInputs, entityType, entityNbt, output, duration);
        }

        @Override
        public AberrantCallingRecipe read(Identifier id, PacketByteBuf buf) {
            final var coreInputs = buf.readCollection(ArrayList::new, Ingredient::fromPacket);
            final var socleInputs = buf.readCollection(ArrayList::new, Ingredient::fromPacket);
            final var output = buf.readItemStack();
            final int duration = buf.readVarInt();

            final var entityType = Registry.ENTITY_TYPE.get(buf.readVarInt());
            final var entityNbt = buf.readOptional(PacketByteBuf::readNbt).orElse(null);

            return new AberrantCallingRecipe(id, coreInputs, socleInputs, entityType, entityNbt, output, duration);
        }

        @Override
        public void write(PacketByteBuf buf, AberrantCallingRecipe recipe) {
            buf.writeCollection(recipe.coreInputs, (packetByteBuf, ingredient) -> ingredient.write(packetByteBuf));
            buf.writeCollection(recipe.inputs, (packetByteBuf, ingredient) -> ingredient.write(packetByteBuf));
            buf.writeItemStack(recipe.output);
            buf.writeVarInt(recipe.duration);

            buf.writeVarInt(Registry.ENTITY_TYPE.getRawId(recipe.entityType));
            buf.writeOptional(Optional.ofNullable(recipe.entityNbt), PacketByteBuf::writeNbt);
        }
    }
}
