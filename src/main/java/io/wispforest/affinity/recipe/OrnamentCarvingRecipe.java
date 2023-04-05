package io.wispforest.affinity.recipe;

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
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

public class OrnamentCarvingRecipe implements Recipe<Inventory> {

    public final Ingredient input;

    private final Identifier id;
    private final ItemStack output;

    public OrnamentCarvingRecipe(Identifier id, Ingredient input, ItemStack output) {
        this.id = id;
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return input.test(inventory.getStack(RitualSocleComposerScreenHandler.ORNAMENT_INGREDIENT_SLOT));
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager drm) {
        return this.output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return width > 0 && height > 0;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager drm) {
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
        return AffinityRecipeTypes.ORNAMENT_CARVING;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    public static final class Serializer implements RecipeSerializer<OrnamentCarvingRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {}

        @Override
        public OrnamentCarvingRecipe read(Identifier id, JsonObject json) {
            var input = Ingredient.fromJson(JsonHelper.getObject(json, "input"));
            var output = JsonHelper.getItem(json, "output").getDefaultStack();
            return new OrnamentCarvingRecipe(id, input, output);
        }

        @Override
        public OrnamentCarvingRecipe read(Identifier id, PacketByteBuf buf) {
            return new OrnamentCarvingRecipe(id, Ingredient.fromPacket(buf), buf.readItemStack());
        }

        @Override
        public void write(PacketByteBuf buf, OrnamentCarvingRecipe recipe) {
            recipe.input.write(buf);
            buf.writeItemStack(recipe.output);
        }
    }
}
