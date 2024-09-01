package io.wispforest.affinity.object;

import com.mojang.serialization.Codec;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Unit;

public class AffinityEnchantmentEffectComponents implements AutoRegistryContainer<ComponentType<?>> {
    public static final ComponentType<Unit> CAUSES_ILLITERACY = ComponentType.<Unit>builder().codec(Unit.CODEC).build();
    public static final ComponentType<Unit> REPAIR_WITH_AFFINE_INFUSER = ComponentType.<Unit>builder().codec(Unit.CODEC).build();
    public static final ComponentType<EnchantmentLevelBasedValue> INCREASES_CRIT_DAMAGE = ComponentType.<EnchantmentLevelBasedValue>builder().codec(EnchantmentLevelBasedValue.CODEC).build();
    public static final ComponentType<EnchantmentLevelBasedValue> INSTANT_KILL_CHANCE = ComponentType.<EnchantmentLevelBasedValue>builder().codec(EnchantmentLevelBasedValue.CODEC).build();
    public static final ComponentType<EnchantmentLevelBasedValue> KILL_TARGET_WHEN_LOW_ON_HEALTH = ComponentType.<EnchantmentLevelBasedValue>builder().codec(EnchantmentLevelBasedValue.CODEC).build();
    public static final ComponentType<Unit> UPDOG_DAMAGE = ComponentType.<Unit>builder().codec(Unit.CODEC).build();

    public static final ComponentType<Integer> ABSOLUTE_NAME_HUE = ComponentType.<Integer>builder().codec(Codec.INT).build();

    @Override
    public Registry<ComponentType<?>> getRegistry() {
        return Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE;
    }

    @Override
    public Class<ComponentType<?>> getTargetFieldType() {
        return AutoRegistryContainer.conform(ComponentType.class);
    }
}
