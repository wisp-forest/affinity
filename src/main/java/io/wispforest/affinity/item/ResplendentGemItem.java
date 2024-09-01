package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Rarity;

public class ResplendentGemItem extends EnchantedBookItem {

    public ResplendentGemItem() {
        super(AffinityItems.settings().rarity(Rarity.UNCOMMON).maxCount(4).component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true));
    }

    public static ItemStack make(RegistryKey<Enchantment> enchantment, RegistryWrapper<Enchantment> enchantments) {
        ItemStack itemStack = new ItemStack(AffinityItems.RESPLENDENT_GEM);

        var entry = enchantments.getOrThrow(enchantment);
        var mutable = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        mutable.add(entry, 1);
        itemStack.set(DataComponentTypes.STORED_ENCHANTMENTS, mutable.build());

        return itemStack;
    }
}
