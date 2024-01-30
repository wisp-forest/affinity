package io.wispforest.affinity.client;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import io.wispforest.lavender.client.LavenderBookScreen;
import io.wispforest.lavender.md.ItemListComponent;
import io.wispforest.lavender.md.compiler.BookCompiler;
import io.wispforest.lavender.md.features.RecipeFeature;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModelLoader;
import me.shedaniel.math.Point;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class AffinityLavenderRecipePreviewBuilders {

    public static final Identifier WISPEN_TESTAMENT_BOOK_ID = Affinity.id("wispen_testament");

    public static void initialize() {
        LavenderBookScreen.registerRecipePreviewBuilder(WISPEN_TESTAMENT_BOOK_ID, AffinityRecipeTypes.ASSEMBLY, new RecipeFeature.RecipePreviewBuilder<>() {
            @Override
            public @NotNull Component buildRecipePreview(BookCompiler.ComponentSource componentSource, RecipeEntry<CraftingRecipe> recipeEntry) {
                var recipe = recipeEntry.value();
                var recipeComponent = componentSource.template(UIModelLoader.get(Affinity.id("wispen_testament")), ParentComponent.class, "assembly-recipe");

                this.populateIngredientsGrid(recipeEntry, recipe.getIngredients(), recipeComponent.childById(ParentComponent.class, "input-grid"), 3, 3);
                recipeComponent.childById(ItemComponent.class, "output").stack(recipe.getResult(MinecraftClient.getInstance().world.getRegistryManager()));

                return recipeComponent;
            }
        });

        LavenderBookScreen.registerRecipePreviewBuilder(WISPEN_TESTAMENT_BOOK_ID, AffinityRecipeTypes.ORNAMENT_CARVING, (componentSource, recipeEntry) -> {
            var recipeComponent = componentSource.template(UIModelLoader.get(Affinity.id("wispen_testament")), ParentComponent.class, "ornament-carving-recipe");

            recipeComponent.childById(ItemListComponent.class, "input").ingredient(recipeEntry.value().input);
            recipeComponent.childById(ItemComponent.class, "output").stack(recipeEntry.value().getResult(null));

            return recipeComponent;
        });

        LavenderBookScreen.registerRecipePreviewBuilder(WISPEN_TESTAMENT_BOOK_ID, AffinityRecipeTypes.ASPEN_INFUSION, (componentSource, recipeEntry) -> {
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

                inputContainer.child(new ItemListComponent()
                        .ingredient(recipeInstance.getIngredients().get(i))
                        .positioning(Positioning.absolute((int) (center.x + Math.cos(angle) * 30), (int) (center.y + Math.sin(angle) * 30)))
                );
            }

            inputContainer.child(Containers.stack(Sizing.content(), Sizing.content())
                    .child(Components.texture(Affinity.id("textures/gui/wispen_testament.png"), 406, 139, 20, 20, 512, 256).blend(true))
                    .child(new ItemListComponent().ingredient(recipeInstance.primaryInput))
                    .horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER));

            // Arrow

            root.child(Containers.verticalFlow(Sizing.fixed(40), Sizing.fill(100)).<FlowLayout>configure(container -> {
                container.horizontalAlignment(HorizontalAlignment.CENTER)
                        .verticalAlignment(VerticalAlignment.BOTTOM);

                container.child(Containers.stack(Sizing.content(), Sizing.content())
                        .child(Components.texture(Affinity.id("textures/gui/wispen_testament.png"), 345, 140, 24, 24, 512, 256).blend(true))
                        .child(Components.item(recipeInstance.getResult(null)).showOverlay(true).setTooltipFromStack(true))
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

        LavenderBookScreen.registerRecipePreviewBuilder(WISPEN_TESTAMENT_BOOK_ID, AffinityRecipeTypes.SPIRIT_ASSIMILATION, (componentSource, recipeEntry) -> {
            var recipeInstance = recipeEntry.value();

            int inputSize = 96;
            var root = Containers.verticalFlow(Sizing.fill(), Sizing.content());
            root.verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER);

            // Input circle

            var inputContainer = Containers.verticalFlow(Sizing.fixed(inputSize), Sizing.fixed(inputSize));
            inputContainer.verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER);
            root.child(inputContainer);

            var center = new Point(inputSize / 2 - 8, inputSize / 2 - 8);
            double angleStep = Math.PI / (recipeInstance.getIngredients().size()) * 2;

            for (int i = 0; i < recipeInstance.getIngredients().size(); i++) {
                double angle = angleStep * i - Math.PI / 2;

                inputContainer.child(new ItemListComponent()
                        .ingredient(recipeInstance.getIngredients().get(i))
                        .positioning(Positioning.absolute((int) (center.x + Math.cos(angle) * 40), (int) (center.y + Math.sin(angle) * 40)))
                );
            }

            inputContainer.child(Containers.stack(Sizing.content(), Sizing.content())
                    .child(Components.texture(Affinity.id("textures/gui/wispen_testament.png"), 452, 139, 37, 37, 512, 256).blend(true))
                    .child(Containers.grid(Sizing.content(), Sizing.content(), 2, 2)
                            .child(new ItemListComponent().ingredient(recipeInstance.coreInputs.get(0)).margins(Insets.of(1)), 0, 0)
                            .child(new ItemListComponent().ingredient(recipeInstance.coreInputs.get(1)), 0, 1)
                            .child(new ItemListComponent().ingredient(recipeInstance.coreInputs.get(2)), 1, 0)
                            .child(new ItemListComponent().ingredient(recipeInstance.coreInputs.get(3)), 1, 1)
                            .horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER))
                    .horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER));

            // Arrow

            root.child(Containers.horizontalFlow(Sizing.fill(), Sizing.fixed(30)).<FlowLayout>configure(container -> {
                container.horizontalAlignment(HorizontalAlignment.LEFT)
                        .verticalAlignment(VerticalAlignment.CENTER);

                container.child((recipeInstance.entityType == EntityType.PLAYER
                        ? Components.entity(Sizing.fixed(25), EntityComponent.createRenderablePlayer(MinecraftClient.getInstance().player.getGameProfile()))
                        : Components.entity(Sizing.fixed(25), recipeInstance.entityType, recipeInstance.entityNbt()))
                        .scaleToFit(true)
                        .tooltip(recipeInstance.entityType.getName())
                        .positioning(Positioning.relative(32, 0))
                );

                container.child(Containers.stack(Sizing.content(), Sizing.content())
                        .child(Components.texture(Affinity.id("textures/gui/wispen_testament.png"), 429, 139, 19, 16, 512, 256)
                                .blend(true)
                                .margins(Insets.right(4)))
                        .child(Components.item(AffinityBlocks.SPIRIT_INTEGRATION_APPARATUS.asItem().getDefaultStack())
                                .setTooltipFromStack(true)
                                .sizing(Sizing.fixed(12))
                                .positioning(Positioning.relative(0, 100))
                                .margins(Insets.of(0, -2, -5, 0)))
                        .allowOverflow(true)
                        .positioning(Positioning.relative(61, 45))
                );

                container.child(Containers.stack(Sizing.content(), Sizing.content())
                        .child(Components.texture(Affinity.id("textures/gui/wispen_testament.png"), 345, 140, 24, 24, 512, 256).blend(true))
                        .child(Components.item(recipeInstance.getResult(null)).showOverlay(true).setTooltipFromStack(true))
                        .horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER)
                        .positioning(Positioning.relative(90, 60))
                );

                if (recipeInstance.fluxCostPerTick != 0) {
                    container.child(Components.label(Text.literal(recipeInstance.fluxCostPerTick * recipeInstance.duration + "\n" + "flux").styled(style -> style.withFont(MinecraftClient.UNICODE_FONT_ID)))
                            .horizontalTextAlignment(HorizontalAlignment.CENTER)
                            .color(Color.ofRgb(0x3f3f3f))
                            .lineHeight(6)
                            .margins(Insets.left(3))
                    );
                }
            }));

            return root;
        });
    }

}
