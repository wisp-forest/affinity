package io.wispforest.affinity.recipe;

import io.wispforest.affinity.misc.screenhandler.RitualSocleComposerScreenHandler;
import io.wispforest.affinity.misc.util.EndecUtil;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.EndecRecipeSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class OrnamentCarvingRecipe implements Recipe<RitualSocleComposerScreenHandler.RecipeInput> {

    public static final StructEndec<OrnamentCarvingRecipe> ENDEC = StructEndecBuilder.of(
            EndecUtil.INGREDIENT_ENDEC.fieldOf("input", recipe -> recipe.input),
            EndecUtil.RECIPE_RESULT_ENDEC.fieldOf("output", recipe -> recipe.output),
            OrnamentCarvingRecipe::new
    );

    public final Ingredient input;
    private final ItemStack output;

    public OrnamentCarvingRecipe(Ingredient input, ItemStack output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(RitualSocleComposerScreenHandler.RecipeInput input, World world) {
        return this.input.test(input.getStackInSlot(RitualSocleComposerScreenHandler.ORNAMENT_INGREDIENT_SLOT));
    }

    @Override
    public ItemStack craft(RitualSocleComposerScreenHandler.RecipeInput input, RegistryWrapper.WrapperLookup registries) {
        return this.output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return width > 0 && height > 0;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registries) {
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

    public static class Serializer extends EndecRecipeSerializer<OrnamentCarvingRecipe> {
        public Serializer() {
            super(OrnamentCarvingRecipe.ENDEC);
        }
    }
}
