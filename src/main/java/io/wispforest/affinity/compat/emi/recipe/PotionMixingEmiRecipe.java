package io.wispforest.affinity.compat.emi.recipe;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.FillingArrowWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.wispforest.affinity.blockentity.impl.BrewingCauldronBlockEntity;
import io.wispforest.affinity.compat.emi.AffinityEmiPlugin;
import io.wispforest.affinity.compat.emi.EmiUIAdapter;
import io.wispforest.affinity.compat.emi.StatusEffectEmiStack;
import io.wispforest.affinity.compat.emi.StatusEffectSlotWidget;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.misc.potion.PotionUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.recipe.PotionMixingRecipe;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.block.Blocks;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.stream.Stream;

public class PotionMixingEmiRecipe extends BasicEmiRecipe {

    private final PotionMixingRecipe recipe;

    public PotionMixingEmiRecipe(Identifier id, PotionMixingRecipe recipe) {
        super(AffinityEmiPlugin.POTION_MIXING, id, 165, 93);
        this.id = id;
        this.recipe = recipe;

        this.inputs = Stream.concat(
                this.recipe.itemInputs.stream().map(AffinityEmiPlugin::veryCoolFeatureYouGotThereEmi),
                this.recipe.effectInputs.stream().map(StatusEffectEmiStack::new)
        ).toList();
        this.outputs = List.of(EmiStack.of(PotionUtil.setPotion(Items.POTION.getDefaultStack(), recipe.potionOutput())));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var bounds = new Bounds(0, 0, widgets.getWidth(), widgets.getHeight());
        var adapter = new EmiUIAdapter<>(bounds, Containers::horizontalFlow);

        var root = adapter.rootComponent();
        root.verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER);
        root.allowOverflow(true);

        // --> Inputs

        var inputContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        root.child(inputContainer.horizontalAlignment(HorizontalAlignment.CENTER));

        var effectContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        inputContainer.child(effectContainer);

        for (var effect : this.recipe.effectInputs) {
            effectContainer.child(adapter.wrap(
                    (x, y) -> new StatusEffectSlotWidget(effect, x, y).drawBack(false)
            ).margins(Insets.of(1)));
        }

        if (!(this.recipe.effectInputs.isEmpty() || this.recipe.itemInputs.isEmpty())) {
            inputContainer.child(
                    Components.box(Sizing.fixed(70), Sizing.fixed(1))
                            .color(Color.ofFormatting(Formatting.GRAY))
                            .margins(Insets.vertical(5))
            );
        }

        var inputs = this.recipe.itemInputs;
        inputs:
        for (int row = 0; row < MathHelper.ceilDiv(inputs.size(), 3); row++) {
            var rowContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            inputContainer.child(rowContainer);

            for (int column = 0; column < 3; column++) {
                int idx = row * 3 + column;
                if (idx >= inputs.size()) break inputs;

                rowContainer.child(adapter.wrap(
                        (x, y) -> AffinityEmiPlugin.slot(EmiIngredient.of(inputs.get(idx)), x, y)
                ).margins(Insets.of(1)));
            }
        }

        // --> Le Arròw

        root.child(adapter.wrap(
                (x, y) -> new FillingArrowWidget(x, y, 5000)
        ).margins(Insets.horizontal(5)));


        // --> Outputs

        var potionNbt = new NbtCompound();
        potionNbt.put(BrewingCauldronBlockEntity.FILL_LEVEL_KEY, 3);
        potionNbt.put(
                BrewingCauldronBlockEntity.STORED_POTION_KEY,
                new PotionMixture(this.recipe.potionOutput(), ComponentMap.EMPTY)
        );

        root.child(
                Containers.verticalFlow(Sizing.content(), Sizing.content())
                        .child(Components.block(Blocks.SPORE_BLOSSOM.getDefaultState())
                                .sizing(Sizing.fixed(40))
                                .margins(Insets.bottom(-10))
                        )
                        .child(adapter.wrap(
                                (x, y) -> AffinityEmiPlugin.slot(this.getOutputs().get(0), x, y).drawBack(false).recipeContext(this)
                        ).margins(Insets.of(0, 5, 0, 1)))
                        .child(Components.block(AffinityBlocks.BREWING_CAULDRON.getDefaultState(), potionNbt)
                                .sizing(Sizing.fixed(40))
                        ).horizontalAlignment(HorizontalAlignment.CENTER)
        );

        adapter.prepare();
        widgets.add(adapter);
    }
}
