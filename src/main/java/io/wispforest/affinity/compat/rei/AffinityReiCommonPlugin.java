package io.wispforest.affinity.compat.rei;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.screenhandler.AssemblyAugmentScreenHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.transfer.info.simple.RecipeBookGridMenuInfo;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleMenuInfoProvider;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;

public class AffinityReiCommonPlugin implements REIServerPlugin {

    public static final CategoryIdentifier<PotionMixingDisplay> POTION_MIXING_ID = CategoryIdentifier.of(Affinity.id("potion_mixing"));
    public static final CategoryIdentifier<DefaultCraftingDisplay<?>> ASSEMBLY = CategoryIdentifier.of(Affinity.id("assembly"));

    @Override
    public void registerMenuInfo(MenuInfoRegistry registry) {
        registry.register(ASSEMBLY, AssemblyAugmentScreenHandler.class, SimpleMenuInfoProvider.of(RecipeBookGridMenuInfo::new));
    }

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(ASSEMBLY, DefaultCraftingDisplay.serializer());
    }
}
