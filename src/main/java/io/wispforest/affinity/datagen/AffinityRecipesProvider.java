package io.wispforest.affinity.datagen;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Consumer;

import static io.wispforest.affinity.object.AffinityItems.*;

public class AffinityRecipesProvider extends FabricRecipeProvider {

    public AffinityRecipesProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateRecipes(Consumer<RecipeJsonProvider> exporter) {
        ShapedRecipeJsonBuilder.create(AETHUM_MAP_PROTOTYPE)
                .pattern(" a ")
                .pattern("ama")
                .pattern(" a ")
                .input('a', ANTHRACITE_POWDER)
                .input('m', Items.MAP)
                .criterion(hasItem(ANTHRACITE_POWDER), conditionsFromItem(ANTHRACITE_POWDER))
                .offerTo(exporter, craftingRecipe(AETHUM_MAP_PROTOTYPE));

        ShapedRecipeJsonBuilder.create(AffinityBlocks.RANTHRACITE_WIRE, 9)
                .pattern("rrr")
                .pattern("rar")
                .pattern("rrr")
                .input('a', ANTHRACITE_POWDER)
                .input('r', Items.REDSTONE)
                .criterion(hasItem(ANTHRACITE_POWDER), conditionsFromItem(ANTHRACITE_POWDER))
                .offerTo(exporter, craftingRecipe(AffinityBlocks.RANTHRACITE_WIRE));

        ShapelessRecipeJsonBuilder.create(Blocks.FLOWERING_AZALEA)
                .input(AZALEA_FLOWERS)
                .input(Blocks.AZALEA)
                .criterion(hasItem(AZALEA_FLOWERS), conditionsFromItem(AZALEA_FLOWERS))
                .offerTo(exporter, craftingRecipe(Blocks.FLOWERING_AZALEA));


        offerChestBoatRecipe(exporter, AZALEA_CHEST_BOAT, AZALEA_BOAT);
    }

    private static Identifier craftingRecipe(ItemConvertible item) {
        final var itemId = Registry.ITEM.getId(item.asItem());
        return new Identifier(Affinity.MOD_ID, "crafting/" + itemId.getPath());
    }
}
