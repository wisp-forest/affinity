package io.wispforest.affinity.client;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import io.wispforest.lavender.client.LavenderBookScreen;
import io.wispforest.lavender.md.compiler.BookCompiler;
import io.wispforest.lavender.md.features.RecipeFeature;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModelLoader;
import me.shedaniel.math.Point;
import net.minecraft.client.MinecraftClient;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class AffinityLavenderRecipeHandlers {

    public static final Identifier WISPEN_TESTAMENT_BOOK_ID = Affinity.id("wispen_testament");

    public static void initialize() {
        LavenderBookScreen.registerRecipeHandler(WISPEN_TESTAMENT_BOOK_ID, AffinityRecipeTypes.ASSEMBLY, new RecipeFeature.RecipeHandler<>() {
            @Override
            public @NotNull Component buildRecipePreview(BookCompiler.ComponentSource componentSource, RecipeEntry<CraftingRecipe> recipeEntry) {
                var recipe = recipeEntry.value();
                var recipeComponent = componentSource.template(UIModelLoader.get(Affinity.id("wispen_testament")), ParentComponent.class, "assembly-recipe");

                this.populateIngredientsGrid(recipeEntry, recipe.getIngredients(), recipeComponent.childById(ParentComponent.class, "input-grid"), 3, 3);
                recipeComponent.childById(ItemComponent.class, "output").stack(recipe.getResult(MinecraftClient.getInstance().world.getRegistryManager()));

                return recipeComponent;
            }
        });

        LavenderBookScreen.registerRecipeHandler(WISPEN_TESTAMENT_BOOK_ID, AffinityRecipeTypes.ASPEN_INFUSION, (componentSource, recipeEntry) -> {
            var recipeInstance = recipeEntry.value();

            int inputSize = 76;
            var root = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(inputSize));
            root.verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER);

            // Input circle

            var inputContainer = Containers.verticalFlow(Sizing.fixed(inputSize), Sizing.fixed(inputSize));
            inputContainer.verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER);
            root.child(inputContainer);

            var center = new Point(inputSize / 2 - 8, inputSize / 2 - 8);
            double angleStep = Math.PI / (recipeInstance.getIngredients().size()) * 2;

            for (int i = 0; i < recipeInstance.getIngredients().size(); i++) {
                double angle = angleStep * i - Math.PI / 2;

                inputContainer.child(new RecipeFeature.IngredientComponent()
                        .ingredient(recipeInstance.getIngredients().get(i))
                        .positioning(Positioning.absolute((int) (center.x + Math.cos(angle) * 30), (int) (center.y + Math.sin(angle) * 30)))
                );
            }

            inputContainer.child(Containers.stack(Sizing.content(), Sizing.content())
                    .child(Components.texture(Affinity.id("textures/gui/wispen_testament.png"), 406, 139, 20, 20, 512, 256).blend(true))
                    .child(new RecipeFeature.IngredientComponent().ingredient(recipeInstance.primaryInput))
                    .horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER));

            // Arrow

            root.child(Containers.verticalFlow(Sizing.content(), Sizing.fill(100)).<FlowLayout>configure(container -> {
                container.horizontalAlignment(HorizontalAlignment.CENTER)
                        .verticalAlignment(VerticalAlignment.BOTTOM)
                        .horizontalSizing(Sizing.fixed(40));

                container.child(Containers.stack(Sizing.content(), Sizing.content())
                        .child(Components.texture(Affinity.id("textures/gui/wispen_testament.png"), 345, 140, 24, 24, 512, 256).blend(true))
                        .child(Components.item(recipeInstance.getResult(null)).setTooltipFromStack(true))
                        .horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER)
                        .positioning(Positioning.relative(50, 2))
                );

                container.child(Containers.stack(Sizing.content(), Sizing.content())
                        .child(Components.texture(Affinity.id("textures/gui/wispen_testament.png"), 386, 139, 16, 19, 512, 256)
                                .blend(true)
                                .margins(Insets.right(4)))
                        .child(Components.item(AffinityBlocks.ASP_RITE_CORE.asItem().getDefaultStack())
                                .setTooltipFromStack(true)
                                .sizing(Sizing.fixed(12))
                                .positioning(Positioning.relative(100, 100))
                                .margins(Insets.of(0, -2, 0, -2)))
                        .allowOverflow(true)
                        .positioning(Positioning.relative(50, 50))
                );

                if (recipeInstance.fluxCostPerTick != 0) {
                    container.child(Components.label(Text.literal(recipeInstance.fluxCostPerTick * recipeInstance.duration + "\n" + "flux").styled(style -> style.withFont(MinecraftClient.UNICODE_FONT_ID)))
                            .horizontalTextAlignment(HorizontalAlignment.CENTER)
                            .color(Color.ofRgb(0x3f3f3f)).lineHeight(6)
                    );
                }
            }));

            return root;
        });
    }

}
