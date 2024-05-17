package io.wispforest.affinity.compat.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.wispforest.affinity.compat.emi.AffinityEmiPlugin;
import io.wispforest.affinity.compat.emi.EmiUIAdapter;
import io.wispforest.affinity.compat.emi.StatusEffectEmiStack;
import io.wispforest.affinity.compat.emi.StatusEffectSlotWidget;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ContainingPotionsEmiRecipe implements EmiRecipe {

    private final StatusEffect effect;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public ContainingPotionsEmiRecipe(StatusEffect effect, List<Potion> potions) {
        this.effect = effect;
        this.inputs = potions.stream()
                .map(x -> {
                    var stack = new ItemStack(Items.POTION);
                    PotionUtil.setPotion(stack, x);
                    return stack;
                })
                .<EmiIngredient>map(EmiStack::of)
                .toList();

        this.outputs = List.of(new StatusEffectEmiStack(effect));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return AffinityEmiPlugin.CONTAINING_POTIONS;
    }

    @Override
    public @Nullable Identifier getId() {
        return null;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return this.inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return this.outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 150;
    }

    @Override
    public int getDisplayHeight() {
        return 66;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var bounds = new Bounds(0, 0, widgets.getWidth(), widgets.getHeight());
        var adapter = new EmiUIAdapter<>(bounds, Containers::verticalFlow);

        var root = adapter.rootComponent();
        root.horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);
        root.allowOverflow(true);

        var contentPane = Containers.verticalFlow(Sizing.content(), Sizing.content());
        contentPane.horizontalAlignment(HorizontalAlignment.CENTER);
        contentPane.margins(Insets.right(4));

        contentPane.child(adapter.wrap(
                (x, y) -> new StatusEffectSlotWidget(this.effect, x, y).drawBack(false)
                ).margins(Insets.bottom(10))
        );

        var inputs = this.getInputs();
        inputs:
        for (int row = 0; row < MathHelper.ceilDiv(inputs.size(), 6); row++) {
            var rowContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            contentPane.child(rowContainer);

            for (int column = 0; column < 6; column++) {
                int idx = row * 6 + column;
                if (idx >= inputs.size()) break inputs;

                rowContainer.child(adapter.wrap(
                        (x, y) -> new SlotWidget(inputs.get(idx), x, y)
                ).margins(Insets.of(1)));
            }
        }

        root.child(Containers.verticalScroll(Sizing.content(), Sizing.fixed(bounds.height() - 8), contentPane)
                .padding(Insets.top(4))
                .margins(Insets.left(4))
        );
        adapter.prepare();
        widgets.add(adapter);
    }
}
