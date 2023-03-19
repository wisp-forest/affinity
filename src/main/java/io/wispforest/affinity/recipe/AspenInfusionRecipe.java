package io.wispforest.affinity.recipe;

import com.google.gson.JsonObject;
import io.wispforest.affinity.blockentity.impl.AspRiteCoreBlockEntity;
import io.wispforest.affinity.misc.util.JsonUtil;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class AspenInfusionRecipe extends RitualRecipe<AspRiteCoreBlockEntity.AspenInfusionInventory> {

    public final Ingredient primaryInput;
    private final ItemStack output;

    public AspenInfusionRecipe(Identifier id, Ingredient primaryInput, List<Ingredient> inputs, ItemStack output, int duration) {
        super(id, inputs, duration);
        this.primaryInput = primaryInput;
        this.output = output;
    }

    @Override
    public boolean matches(AspRiteCoreBlockEntity.AspenInfusionInventory inventory, World world) {
        return this.primaryInput.test(inventory.primaryInput()) && this.doShapelessMatch(this.socleInputs, inventory.delegate());
    }

    @Override
    public ItemStack craft(AspRiteCoreBlockEntity.AspenInfusionInventory inventory) {
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
        return AffinityRecipeTypes.ASPEN_INFUSION;
    }

    public static final class Serializer implements RecipeSerializer<AspenInfusionRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {}

        @Override
        public AspenInfusionRecipe read(Identifier id, JsonObject json) {
            final var output = JsonUtil.readChadStack(json, "output");
            final var baseInput = Ingredient.fromJson(JsonHelper.getObject(json, "primary_input"));
            final var inputs = JsonUtil.readIngredientList(json, "inputs");

            final int duration = JsonHelper.getInt(json, "duration", 100);

            return new AspenInfusionRecipe(id, baseInput, inputs, output, duration);
        }

        @Override
        public AspenInfusionRecipe read(Identifier id, PacketByteBuf buf) {
            final var baseInput = Ingredient.fromPacket(buf);
            final var inputs = buf.readCollection(ArrayList::new, Ingredient::fromPacket);
            final var output = buf.readItemStack();
            final int duration = buf.readVarInt();
            return new AspenInfusionRecipe(id, baseInput, inputs, output, duration);
        }

        @Override
        public void write(PacketByteBuf buf, AspenInfusionRecipe recipe) {
            recipe.primaryInput.write(buf);
            buf.writeCollection(recipe.socleInputs, (packetByteBuf, ingredient) -> ingredient.write(packetByteBuf));
            buf.writeItemStack(recipe.output);
            buf.writeVarInt(recipe.duration);
        }
    }
}
