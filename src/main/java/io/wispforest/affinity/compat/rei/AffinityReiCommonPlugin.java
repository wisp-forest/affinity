package io.wispforest.affinity.compat.rei;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.compat.rei.display.*;
import io.wispforest.affinity.misc.screenhandler.AssemblyAugmentScreenHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.transfer.info.simple.RecipeBookGridMenuInfo;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleMenuInfoProvider;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.minecraft.entity.effect.StatusEffect;

import java.util.stream.IntStream;

public class AffinityReiCommonPlugin implements REIServerPlugin {

    public static final CategoryIdentifier<PotionMixingDisplay> POTION_MIXING = CategoryIdentifier.of(Affinity.id("potion_mixing"));
    public static final CategoryIdentifier<ContainingPotionsDisplay> CONTAINING_POTIONS = CategoryIdentifier.of(Affinity.id("containing_potions"));
    public static final CategoryIdentifier<AspenInfusionDisplay> ASPEN_INFUSION = CategoryIdentifier.of(Affinity.id("aspen_infusion"));
    public static final CategoryIdentifier<SpiritAssimilationDisplay> SPIRIT_ASSIMILATION = CategoryIdentifier.of(Affinity.id("spirit_assimilation"));
    public static final CategoryIdentifier<DefaultCraftingDisplay<?>> ASSEMBLY = CategoryIdentifier.of(Affinity.id("assembly"));
    public static final CategoryIdentifier<ArcaneFadingDisplay> ARCANE_FADING = CategoryIdentifier.of(Affinity.id("arcane_fading"));
    public static final CategoryIdentifier<OrnamentCarvingDisplay> ORNAMENT_CARVING = CategoryIdentifier.of(Affinity.id("ornament_carving"));
    public static final CategoryIdentifier<SocleComposingDisplay> SOCLE_COMPOSING = CategoryIdentifier.of(Affinity.id("socle_composing"));

    public static final EntryType<StatusEffect> EFFECT_ENTRY_TYPE = EntryType.deferred(Affinity.id("status_effect"));

    @Override
    public void registerMenuInfo(MenuInfoRegistry registry) {
        registry.register(ASSEMBLY, AssemblyAugmentScreenHandler.class, SimpleMenuInfoProvider.of(AssemblyAugmentMenuInfo::new));
    }

    @Override
    public void registerEntryTypes(EntryTypeRegistry registry) {
        registry.register(EFFECT_ENTRY_TYPE, new StatusEffectEntryDefinition());
    }

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(ASSEMBLY, DefaultCraftingDisplay.serializer());
    }

    public static class AssemblyAugmentMenuInfo extends RecipeBookGridMenuInfo<AssemblyAugmentScreenHandler, DefaultCraftingDisplay<?>> {

        public AssemblyAugmentMenuInfo(DefaultCraftingDisplay<?> display) {
            super(display);
        }

        @Override
        public IntStream getInputStackSlotIds(MenuInfoContext<AssemblyAugmentScreenHandler, ?, DefaultCraftingDisplay<?>> context) {
            return IntStream.concat(super.getInputStackSlotIds(context), IntStream.of(context.getMenu().slots.get(context.getMenu().slots.size() - 1).id));
        }
    }
}
