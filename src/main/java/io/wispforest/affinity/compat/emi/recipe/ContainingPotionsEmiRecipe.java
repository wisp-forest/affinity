package io.wispforest.affinity.compat.emi.recipe;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
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
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;

import java.util.List;

public class ContainingPotionsEmiRecipe extends BasicEmiRecipe {

    public ContainingPotionsEmiRecipe(StatusEffect effect, Potion potion) {
        super(AffinityEmiPlugin.CONTAINING_POTIONS, null, 100, 24);

        this.inputs = List.of(EmiStack.of(PotionUtil.setPotion(new ItemStack(Items.POTION), potion)));
        this.outputs = List.of(new StatusEffectEmiStack(effect));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var bounds = new Bounds(0, 0, widgets.getWidth(), widgets.getHeight());
        var adapter = new EmiUIAdapter<>(bounds, Containers::horizontalFlow);

        var root = adapter.rootComponent();

        root.gap(6).horizontalAlignment(HorizontalAlignment.CENTER).verticalAlignment(VerticalAlignment.CENTER);

        root.child(adapter.wrap((x, y) -> AffinityEmiPlugin.slot(this.inputs.get(0), x, y)));
        root.child(adapter.wrap(AffinityEmiPlugin::arrow));
        root.child(adapter.wrap((x, y) -> new StatusEffectSlotWidget(this.outputs.get(0), x, y).drawBack(false).recipeContext(this)));

        adapter.prepare();
        widgets.add(adapter);
    }
}
