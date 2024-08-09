package io.wispforest.affinity.enchantment;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class CriticalGambleEnchantmentLogic {
    public static final TagKey<EntityType<?>> BLACKLIST = TagKey.of(RegistryKeys.ENTITY_TYPE, Affinity.id("critical_gamble_blacklist"));
    public static final AffinityEntityAddon.DataKey<Long> ACTIVATED_AT = AffinityEntityAddon.DataKey.withDefaultConstant(-1L);
}
