package io.wispforest.affinity.misc.recipe;

import com.google.gson.JsonObject;
import io.wispforest.affinity.misc.screenhandler.RitualSocleComposerScreenHandler;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

public class RitualSocleComposerConversionRecipe implements Recipe<Inventory> {

    private final Identifier id;
    private final Ingredient input;
    private final ItemStack output;

    public RitualSocleComposerConversionRecipe(Identifier id, Ingredient input, ItemStack output) {
        this.id = id;
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return input.test(inventory.getStack(RitualSocleComposerScreenHandler.ORNAMENT_INGREDIENT_SLOT));
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        return this.output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return width > 0 && height > 0;
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
        return AffinityRecipeTypes.RITUAL_SOCLE_COMPOSER_CONVERSION;
    }

    public static final class Serializer implements RecipeSerializer<RitualSocleComposerConversionRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {}

        @Override
        public RitualSocleComposerConversionRecipe read(Identifier id, JsonObject json) {
            var input = Ingredient.fromJson(JsonHelper.getObject(json, "input"));
            var output = JsonHelper.getItem(json, "output").getDefaultStack();
            return new RitualSocleComposerConversionRecipe(id, input, output);
        }

        @Override
        public RitualSocleComposerConversionRecipe read(Identifier id, PacketByteBuf buf) {
            return new RitualSocleComposerConversionRecipe(id, Ingredient.fromPacket(buf), buf.readItemStack());
        }

        @Override
        public void write(PacketByteBuf buf, RitualSocleComposerConversionRecipe recipe) {
            recipe.input.write(buf);
            buf.writeItemStack(recipe.output);
        }
    }
}
