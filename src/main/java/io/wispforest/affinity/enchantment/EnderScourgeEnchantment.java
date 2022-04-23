package io.wispforest.affinity.enchantment;

import io.wispforest.affinity.Affinity;
import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.entity.*;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

public class EnderScourgeEnchantment extends DamageEnchantment {

    public static final TagKey<EntityType<?>> END_ENTITIES = TagKey.of(Registry.ENTITY_TYPE_KEY, Affinity.id("end_entities"));

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
}
