package io.wispforest.affinity.object;

import io.wispforest.affinity.enchantment.IlliteracyCurse;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.registry.Registry;

public class AffinityEnchantments implements AutoRegistryContainer<Enchantment> {

    public static final Enchantment CURSE_OF_ILLITERACY = new IlliteracyCurse();

    @Override
    public Registry<Enchantment> getRegistry() {
        return Registry.ENCHANTMENT;
    }

    @Override
    public Class<Enchantment> getTargetFieldType() {
        return Enchantment.class;
    }
}
