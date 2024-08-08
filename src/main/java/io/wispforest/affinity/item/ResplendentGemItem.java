package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Rarity;

public class ResplendentGemItem extends EnchantedBookItem {

    public ResplendentGemItem() {
        super(AffinityItems.settings().rarity(Rarity.UNCOMMON).maxCount(1)
                .stackGenerator(ResplendentGemItem::generateStacks));
    }

    public static ItemStack make(RegistryKey<Enchantment> enchantment, RegistryWrapper.WrapperLookup registries) {
        ItemStack itemStack = new ItemStack(AffinityItems.RESPLENDENT_GEM);

        var entry = registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(enchantment);
        var mutable = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        mutable.add(entry, 1);
        itemStack.set(DataComponentTypes.STORED_ENCHANTMENTS, mutable.build());

        return itemStack;
    }

    private static void generateStacks(Item item, ItemGroup.Entries stacks) {
        var registries = MinecraftClient.getInstance().world.getRegistryManager();

        // TODO: port this to the absolute enchantment tag.
//        registries.get(RegistryKeys.ENCHANTMENT).stream()
//                .filter(AbsoluteEnchantment.class::isInstance)
//                .map(AbsoluteEnchantment.class::cast)
//                .forEach(enchantment -> stacks.add(make(enchantment)));
    }
}
