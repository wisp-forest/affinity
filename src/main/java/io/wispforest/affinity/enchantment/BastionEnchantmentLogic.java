package io.wispforest.affinity.enchantment;

import io.wispforest.affinity.misc.callback.ItemEquipEvents;
import io.wispforest.affinity.misc.callback.LivingEntityTickCallback;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.object.AffinityEnchantments;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;

public class BastionEnchantmentLogic {
    public static final AffinityEntityAddon.DataKey<Boolean> BASTION = AffinityEntityAddon.DataKey.withDefaultConstant(true);

    public static void initialize() {
        ItemEquipEvents.EQUIP.register((entity, slot, stack) -> {
            var ench = entity.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(AffinityEnchantments.BASTION).orElse(null);
            if (ench == null) return;

            if (EnchantmentHelper.getLevel(ench, stack) == 0) return;

            if (!ench.value().slotMatches(slot) || !AbsoluteEnchantmentLogic.hasCompleteArmor(entity, ench)) return;
            AffinityEntityAddon.createDefaultData(entity, BASTION);
        });

        ItemEquipEvents.UNEQUIP.register((entity, slot, stack) -> {
            var ench = entity.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(AffinityEnchantments.BASTION).orElse(null);
            if (ench == null || EnchantmentHelper.getLevel(ench, stack) == 0) return;

            if (!ench.value().slotMatches(slot)) return;
            AffinityEntityAddon.removeData(entity, BASTION);
        });

        LivingEntityTickCallback.EVENT.register(entity -> {
            if (!AffinityEntityAddon.hasData(entity, BASTION)) return;

            if (entity.getWorld().getTime() % 10 == 0) {
                entity.addStatusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(AffinityStatusEffects.BASTION_REGENERATION), 15, 0, true, false));
            }
        });
    }
}
