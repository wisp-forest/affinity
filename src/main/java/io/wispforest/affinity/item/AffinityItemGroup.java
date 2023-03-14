package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.enchantment.template.AbsoluteEnchantment;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class AffinityItemGroup {

    public static final int MAIN = 0;
    public static final int NATURE = 1;
    public static final int EQUIPMENT = 2;
    public static final int ENCHANTMENTS = 3;

    public static final OwoItemGroup GROUP = OwoItemGroup.builder(Affinity.id("affinity"), () -> Icon.of(AffinityItems.INERT_WISP_MATTER)).initializer(group -> {
        group.addTab(Icon.of(AffinityItems.EMERALD_WAND_OF_IRIDESCENCE), "main", null, true);
        group.addTab(Icon.of(AffinityBlocks.AZALEA_LOG), "nature", null, false);
        group.addTab(Icon.of(AffinityItems.RESOUNDING_CHIME), "equipment", null, false);
        group.addCustomTab(Icon.of(AffinityItems.RESPLENDENT_GEM), "enchantments", (enabledFeatures, entries, hasPermissions) -> {
            Registries.ENCHANTMENT.getIds().stream()
                    .filter(id -> id.getNamespace().equals(Affinity.MOD_ID))
                    .map(Registries.ENCHANTMENT::get)
                    .filter(enchantment -> !(enchantment instanceof AbsoluteEnchantment))
                    .map(enchantment -> new EnchantmentLevelEntry(enchantment, enchantment.getMaxLevel()))
                    .map(EnchantedBookItem::forEnchantment)
                    .forEach(entries::add);
        }, false);

        group.addButton(ItemGroupButton.github(group, "https://github.com/wisp-forest/affinity"));
    }).build();

    static {
        ItemGroupEvents.modifyEntriesEvent(GROUP).register(entries -> {
            if (GROUP.getSelectedTabIndex() != 0) return;
            entries.addBefore(AffinityItems.MILDLY_ATTUNED_AMETHYST_SHARD, Items.AMETHYST_SHARD);
        });
    }
}
