package io.wispforest.affinity.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.affinity.misc.screenhandler.RitualSocleComposerScreenHandler;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;

public class OrnamentCarvingRecipe implements Recipe<Inventory> {

    public static final Codec<OrnamentCarvingRecipe> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
                    Registries.ITEM.getCodec().xmap(Item::getDefaultStack, ItemStack::getItem).fieldOf("output").forGetter(recipe -> recipe.output)
            )
            .apply(instance, OrnamentCarvingRecipe::new));

    public final Ingredient input;
    private final ItemStack output;

    public OrnamentCarvingRecipe(Ingredient input, ItemStack output) {
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
    public ItemStack getResult(DynamicRegistryManager drm) {
        return this.output.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AffinityRecipeTypes.Serializers.ORNAMENT_CARVING;
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

        public Serializer() {}

        @Override
        public Codec<OrnamentCarvingRecipe> codec() {
            return OrnamentCarvingRecipe.CODEC;
        }

        @Override
        public OrnamentCarvingRecipe read(PacketByteBuf buf) {
            return new OrnamentCarvingRecipe(Ingredient.fromPacket(buf), buf.readItemStack());
        }

        @Override
        public void write(PacketByteBuf buf, OrnamentCarvingRecipe recipe) {
            recipe.input.write(buf);
            buf.writeItemStack(recipe.output);
        }
    }
}
