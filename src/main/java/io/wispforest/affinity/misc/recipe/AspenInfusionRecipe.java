package io.wispforest.affinity.misc.recipe;

import com.google.gson.JsonObject;
import io.wispforest.affinity.blockentity.template.RitualCoreBlockEntity;
import io.wispforest.affinity.misc.util.JsonUtil;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class AspenInfusionRecipe implements Recipe<RitualCoreBlockEntity.SocleInventory> {

    private final Identifier id;
    private final List<Ingredient> inputs;
    private final ItemStack output;

    public AspenInfusionRecipe(Identifier id, List<Ingredient> inputs, ItemStack output) {
        this.id = id;
        this.inputs = inputs;
        this.output = output;
    }

    @Override
    public boolean matches(RitualCoreBlockEntity.SocleInventory inventory, World world) {
        final var matcher = new RecipeMatcher();
        int nonEmptyStacks = 0;

        for (int j = 0; j < inventory.size(); ++j) {
            ItemStack itemStack = inventory.getStack(j);
            if (!itemStack.isEmpty()) {
                ++nonEmptyStacks;
                matcher.addInput(itemStack, 1);
            }
        }

        return nonEmptyStacks == this.inputs.size() && matcher.match(this, null);
    }

    @Override
    public ItemStack craft(RitualCoreBlockEntity.SocleInventory inventory) {
        return this.output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getOutput() {
        return this.output.copy();
    }

    @Override
    public Identifier getId() {
        return this.id;
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
            final var inputs = new ArrayList<Ingredient>();

            for (var inputElement : JsonHelper.getArray(json, "inputs")) {
                inputs.add(Ingredient.fromJson(inputElement));
            }

            return new AspenInfusionRecipe(id, inputs, output);
        }

        @Override
        public AspenInfusionRecipe read(Identifier id, PacketByteBuf buf) {
            final var inputs = buf.readCollection(ArrayList::new, Ingredient::fromPacket);
            final var output = buf.readItemStack();
            return new AspenInfusionRecipe(id, inputs, output);
        }

        @Override
        public void write(PacketByteBuf buf, AspenInfusionRecipe recipe) {
            buf.writeCollection(recipe.inputs, (packetByteBuf, ingredient) -> ingredient.write(packetByteBuf));
            buf.writeItemStack(recipe.output);
        }
    }
}
