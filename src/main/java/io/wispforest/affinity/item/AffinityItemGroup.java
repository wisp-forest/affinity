package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.enchantment.template.AbsoluteEnchantment;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

public class AffinityItemGroup extends OwoItemGroup {

    public static final int MAIN = 0;
    public static final int NATURE = 1;
    public static final int ENCHANTMENTS = 2;

    public AffinityItemGroup(Identifier id) {
        super(id);
    }

    @Override
    protected void setup() {
        this.addTab(Icon.of(AffinityItems.EMERALD_WAND_OF_IRIDESCENCE), "main", null, true);
        this.addTab(Icon.of(AffinityBlocks.AZALEA_LOG), "nature", null, false);
        this.addTab(Icon.of(AffinityItems.RESPLENDENT_GEM), "enchantments", null, false);

        this.addButton(ItemGroupButton.github("https://github.com/wisp-forest/affinity"));
    }

    @Override
    public void appendStacks(DefaultedList<ItemStack> stacks) {
        super.appendStacks(stacks);

        if (this.getSelectedTabIndex() == 0) {
            for (int i = 0; i < stacks.size(); i++) {
                if (!stacks.get(i).isOf(AffinityItems.MILDLY_ATTUNED_AMETHYST_SHARD)) continue;
                stacks.add(i, Items.AMETHYST_SHARD.getDefaultStack());
                break;
            }
        }

        if (this.getSelectedTabIndex() == 2) {
            Registry.ENCHANTMENT.getIds().stream()
                    .filter(id -> id.getNamespace().equals(Affinity.MOD_ID))
                    .map(Registry.ENCHANTMENT::get)
                    .filter(enchantment -> !(enchantment instanceof AbsoluteEnchantment))
                    .map(enchantment -> new EnchantmentLevelEntry(enchantment, enchantment.getMaxLevel()))
                    .map(EnchantedBookItem::forEnchantment)
                    .forEach(stack -> stacks.add(0, stack));
        }
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(AffinityItems.WISE_WISP_MATTER);
    }
}
