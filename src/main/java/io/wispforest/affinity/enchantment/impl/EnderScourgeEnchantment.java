package io.wispforest.affinity.enchantment.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.enchantment.template.AffinityDamageEnchantment;
import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.entity.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class EnderScourgeEnchantment extends DamageEnchantment implements AffinityDamageEnchantment {

    public static final TagKey<EntityType<?>> END_ENTITIES = TagKey.of(RegistryKeys.ENTITY_TYPE, Affinity.id("end_entities"));

    public EnderScourgeEnchantment() {
        super(Rarity.RARE, -1, EquipmentSlot.MAINHAND);
    }

    @Override
    public int getMinPower(int level) {
        return 5 + (level - 1) * 8;
    }

    @Override
    public int getMaxPower(int level) {
        return this.getMinPower(level) + 20;
    }

    @Override
    public float getAttackDamage(int level, EntityGroup group) {
        return 0;
    }

    @Override
    public void onTargetDamaged(LivingEntity user, Entity target, int level) {}

    @Override
    public boolean shouldApplyDamage(int level, LivingEntity attacker, LivingEntity target, float incomingDamage) {
        return target.getType().isIn(END_ENTITIES);
    }

    @Override
    public float getExtraDamage(int level, LivingEntity attacker, LivingEntity target, float incomingDamage) {
        return level * 2.5f;
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return false;
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return false;
    }

}
