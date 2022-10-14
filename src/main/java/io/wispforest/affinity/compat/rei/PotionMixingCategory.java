package io.wispforest.affinity.compat.rei;

import io.wispforest.affinity.blockentity.impl.BrewingCauldronBlockEntity;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.compat.rei.ReiUIAdapter;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.*;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class PotionMixingCategory implements DisplayCategory<PotionMixingDisplay> {

    @Override
    public List<Widget> setupDisplay(PotionMixingDisplay display, Rectangle bounds) {
        var adapter = new ReiUIAdapter<>(bounds, Containers::horizontalFlow);
        var root = adapter.rootComponent();
        root.verticalAlignment(VerticalAlignment.CENTER).horizontalAlignment(HorizontalAlignment.CENTER);
        root.allowOverflow(true);

        root.child(adapter.wrap(Widgets.createRecipeBase(bounds)).positioning(Positioning.absolute(0, 0)));

        // --> Inputs

        var inputContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        root.child(inputContainer.horizontalAlignment(HorizontalAlignment.CENTER));

        var effectContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        inputContainer.child(effectContainer);

        for (var effect : display.getEffects()) {
            var ingredient = EntryIngredients.of(AffinityReiCommonPlugin.EFFECT_ENTRY_TYPE, List.of(effect));

            effectContainer.child(adapter.wrap(
                Widgets::createSlot,
                slot -> slot.entries(ingredient).markInput().disableBackground().disableHighlight()
            ).margins(Insets.of(1)));
        }

        if (!(display.getEffects().isEmpty() || display.getRecipe().getItemInputs().isEmpty())) {
            inputContainer.child(
                    Components.box(Sizing.fixed(70), Sizing.fixed(1))
                            .color(Color.ofFormatting(Formatting.GRAY))
                            .margins(Insets.vertical(5))
            );
        }

        var inputs = display.getRecipe().getItemInputs();
        inputs:
        for (int row = 0; row < MathHelper.ceilDiv(inputs.size(), 3); row++) {
            var rowContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            inputContainer.child(rowContainer);

            for (int column = 0; column < 3; column++) {
                int idx = row * 3 + column;
                if (idx >= inputs.size()) break inputs;

                rowContainer.child(adapter.wrap(
                        Widgets::createSlot,
                        slot -> slot.entries(EntryIngredients.ofIngredient(inputs.get(idx).itemPredicate())).markInput()
                ).margins(Insets.of(1)));
            }
        }

        // --> Le Arròw

        root.child(adapter.wrap(
                Widgets::createArrow,
                arrow -> arrow.animationDurationTicks(100)
        ).margins(Insets.horizontal(5)));


        // --> Outputs

        var potionNbt = new NbtCompound();
        potionNbt.put(BrewingCauldronBlockEntity.FILL_LEVEL_KEY, 3);
        potionNbt.put(
                BrewingCauldronBlockEntity.STORED_POTION_KEY,
                new PotionMixture(display.getPotionOutput(), null)
        );

        root.child(
                Containers.verticalFlow(Sizing.content(), Sizing.content())
                        .child(Components.block(Blocks.SPORE_BLOSSOM.getDefaultState())
                                .sizing(Sizing.fixed(40))
                                .margins(Insets.bottom(-10))
                        )
                        .child(adapter.wrap(
                                Widgets::createSlot,
                                slot -> slot.entries(display.getOutputEntries().get(0)).markOutput().disableBackground()
                        ).margins(Insets.of(0, 5, 0, 1)))
                        .child(Components.block(AffinityBlocks.BREWING_CAULDRON.getDefaultState(), potionNbt)
                                .sizing(Sizing.fixed(40))
                        ).horizontalAlignment(HorizontalAlignment.CENTER)
        );

        adapter.prepare();
        return List.of(adapter);
    }

    @Override
    public int getDisplayHeight() {
        return 105;
    }

    @Override
    public int getDisplayWidth(PotionMixingDisplay display) {
        return 165;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AffinityBlocks.BREWING_CAULDRON);
    }

    @Override
    public Text getTitle() {
        return Text.translatable("category.affinity.potion_mixing");
    }

    @Override
    public CategoryIdentifier<? extends PotionMixingDisplay> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.POTION_MIXING;
    }
}
