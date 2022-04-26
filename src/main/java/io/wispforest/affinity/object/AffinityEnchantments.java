package io.wispforest.affinity.object;

import io.wispforest.affinity.enchantment.impl.*;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.registry.Registry;

public class AffinityEnchantments implements AutoRegistryContainer<Enchantment> {

    public static final IlliteracyCurseEnchantment CURSE_OF_ILLITERACY = new IlliteracyCurseEnchantment();
    public static final HealthCurseEnchantment CURSE_OF_HEALTH = new HealthCurseEnchantment();
    public static final AffineEnchantment AFFINE = new AffineEnchantment();
    public static final EnderScourgeEnchantment ENDER_SCOURGE = new EnderScourgeEnchantment();
    public static final BerserkerEnchantment BERSERKER = new BerserkerEnchantment();
    public static final GravecallerEnchantment GRAVECALLER = new GravecallerEnchantment();

    @Override
    public Registry<Enchantment> getRegistry() {
        return Registry.ENCHANTMENT;
    }

    @Override
    public Class<Enchantment> getTargetFieldType() {
        return Enchantment.class;
    }
}
