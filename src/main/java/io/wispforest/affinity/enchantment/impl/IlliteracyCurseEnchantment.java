package io.wispforest.affinity.enchantment.impl;

import io.wispforest.affinity.enchantment.template.EnchantmentEquipEventReceiver;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.network.AffinityNetwork;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class IlliteracyCurseEnchantment extends CurseEnchantment implements EnchantmentEquipEventReceiver {

    public IlliteracyCurseEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.ARMOR_HEAD, EquipmentSlot.HEAD);
    }

    @Override
    public void onEquip(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.HEAD || !(entity instanceof ServerPlayerEntity player)) return;
        AffinityNetwork.server(player).send(new IlliteracyPacket(true));
    }

    @Override
    public void onUnequip(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.HEAD || !(entity instanceof ServerPlayerEntity player)) return;
        AffinityNetwork.server(player).send(new IlliteracyPacket(false));
    }

    static {
        AffinityNetwork.CHANNEL.registerClientbound(IlliteracyPacket.class, (message, access) -> {
            MixinHooks.TEXT_OBFUSCATION = message.illiterate;
        });
    }

    public record IlliteracyPacket(boolean illiterate) {}
}
