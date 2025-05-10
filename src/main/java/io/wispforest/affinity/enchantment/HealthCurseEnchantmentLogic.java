package io.wispforest.affinity.enchantment;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.callback.ItemEquipEvents;
import io.wispforest.affinity.object.AffinityEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.RegistryKeys;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class HealthCurseEnchantmentLogic {
        private static final EntityAttributeModifier HEALTH_ADDITION = new EntityAttributeModifier(
            Affinity.id("curse_of_health_boost"), 4, EntityAttributeModifier.Operation.ADD_VALUE);

    public static void initialize() {
        ItemEquipEvents.EQUIP.register((entity, slot, stack) -> {
            var ench = entity.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(AffinityEnchantments.CURSE_OF_HEALTH).orElse(null);
            if (ench == null) return;

            if (EnchantmentHelper.getLevel(ench, stack) == 0) return;

            if (!ench.value().slotMatches(slot)) return;

            if (!healthAttribute(entity).hasModifier(HEALTH_ADDITION.id())) {
                healthAttribute(entity).addTemporaryModifier(HEALTH_ADDITION);
            }
        });

        ItemEquipEvents.UNEQUIP.register((entity, slot, stack) -> {
            var enchantment = entity.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(AffinityEnchantments.CURSE_OF_HEALTH).orElse(null);
            if (enchantment == null || EnchantmentHelper.getLevel(enchantment, stack) == 0) return;

            if (!enchantment.value().slotMatches(slot)) return;

            healthAttribute(entity).removeModifier(HEALTH_ADDITION.id());

            healthAttribute(entity).addPersistentModifier(new EntityAttributeModifier(
                    Affinity.id("curse_of_health_penalty_" + ThreadLocalRandom.current().nextInt()), -10, EntityAttributeModifier.Operation.ADD_VALUE));
            entity.damage(entity.getDamageSources().outOfWorld(), Float.MIN_NORMAL);
        });
    }

    private static EntityAttributeInstance healthAttribute(LivingEntity entity) {
        return entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
    }
}
