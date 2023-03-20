package io.wispforest.affinity.compat.rei.display;

import io.wispforest.affinity.compat.rei.AffinityReiCommonPlugin;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.util.Util;

import java.util.List;

public class SocleComposingDisplay extends BasicDisplay {

    public SocleComposingDisplay(RitualSocleType type, Action action) {
        super(Util.make(() -> switch (action) {
            case CRAFT -> List.of(EntryIngredients.of(AffinityBlocks.BLANK_RITUAL_SOCLE), EntryIngredients.of(type.ornamentItem()));
            case UNCRAFT -> List.of(EntryIngredients.of(type.socleBlock()));
        }), Util.make(() -> switch (action) {
            case CRAFT -> List.of(EntryIngredients.of(type.socleBlock()));
            case UNCRAFT -> List.of(EntryIngredients.of(AffinityBlocks.BLANK_RITUAL_SOCLE), EntryIngredients.of(type.ornamentItem()));
        }));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AffinityReiCommonPlugin.SOCLE_COMPOSING;
    }

    public enum Action {
        CRAFT, UNCRAFT;
    }
}
