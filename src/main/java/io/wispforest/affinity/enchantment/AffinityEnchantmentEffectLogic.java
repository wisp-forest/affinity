package io.wispforest.affinity.enchantment;

import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.callback.ItemEquipEvents;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityEnchantmentEffectComponents;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.server.network.ServerPlayerEntity;

public final class AffinityEnchantmentEffectLogic {
    private AffinityEnchantmentEffectLogic() { }

    public static void initialize() {
        AffinityNetwork.CHANNEL.registerClientbound(IlliteracyPacket.class, (message, access) -> {
            MixinHooks.textObfuscation = message.illiterate;
        });

        ItemEquipEvents.EQUIP.register((entity, slot, stack) -> {
            if (slot != EquipmentSlot.HEAD || !(entity instanceof ServerPlayerEntity player)) return;
            if (!EnchantmentHelper.hasAnyEnchantmentsWith(stack, AffinityEnchantmentEffectComponents.CAUSES_ILLITERACY)) return;

            AffinityNetwork.server(player).send(new IlliteracyPacket(true));
        });

        ItemEquipEvents.UNEQUIP.register((entity, slot, stack) -> {
            if (slot != EquipmentSlot.HEAD || !(entity instanceof ServerPlayerEntity player)) return;
            if (!EnchantmentHelper.hasAnyEnchantmentsWith(stack, AffinityEnchantmentEffectComponents.CAUSES_ILLITERACY)) return;

            AffinityNetwork.server(player).send(new IlliteracyPacket(false));
        });
    }

    public record IlliteracyPacket(boolean illiterate) {}
}
