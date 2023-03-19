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

    public AspenInfusionRecipe(Identifier id, Ingredient primaryInput, List<Ingredient> inputs, ItemStack output, int duration, int fluxCostPerTick) {
        super(id, inputs, duration, fluxCostPerTick);
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
            return new AspenInfusionRecipe(id,
                    Ingredient.fromJson(JsonHelper.getObject(json, "primary_input")),
                    JsonUtil.readIngredientList(json, "inputs"),
                    JsonUtil.readChadStack(json, "output"),
                    JsonHelper.getInt(json, "duration", 100),
                    JsonHelper.getInt(json, "flux_cost_per_tick", 0)
            );
        }

        @Override
        public AspenInfusionRecipe read(Identifier id, PacketByteBuf buf) {
            return new AspenInfusionRecipe(
                    id,
                    Ingredient.fromPacket(buf),
                    buf.readCollection(ArrayList::new, Ingredient::fromPacket),
                    buf.readItemStack(),
                    buf.readVarInt(),
                    buf.readVarInt()
            );
        }

        @Override
        public void write(PacketByteBuf buf, AspenInfusionRecipe recipe) {
            recipe.primaryInput.write(buf);
            buf.writeCollection(recipe.socleInputs, (packetByteBuf, ingredient) -> ingredient.write(packetByteBuf));
            buf.writeItemStack(recipe.output);
            buf.writeVarInt(recipe.duration);
            buf.writeVarInt(recipe.fluxCostPerTick);
        }
    }
}
