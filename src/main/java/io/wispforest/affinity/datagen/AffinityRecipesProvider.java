package io.wispforest.affinity.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipesProvider;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonFactory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Consumer;

import static io.wispforest.affinity.object.AffinityItems.AETHUM_MAP_PROTOTYPE;
import static io.wispforest.affinity.object.AffinityItems.ANTHRACITE_POWDER;

public class AffinityRecipesProvider extends FabricRecipesProvider {

    public AffinityRecipesProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateRecipes(Consumer<RecipeJsonProvider> exporter) {
        ShapedRecipeJsonFactory.create(AETHUM_MAP_PROTOTYPE)
                .pattern(" a ")
                .pattern("ama")
                .pattern(" a ")
                .input('a', ANTHRACITE_POWDER)
                .input('m', Items.MAP)
                .criterion(hasItem(ANTHRACITE_POWDER), conditionsFromItem(ANTHRACITE_POWDER))
                .offerTo(exporter, craftingRecipe(AETHUM_MAP_PROTOTYPE));
    }

    private static Identifier craftingRecipe(ItemConvertible item) {
        final var itemId = Registry.ITEM.getId(item.asItem());
        return new Identifier(itemId.getNamespace(), "crafting/" + itemId.getPath());
    }
}
