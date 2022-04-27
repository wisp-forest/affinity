package io.wispforest.affinity.mixin;

import io.wispforest.affinity.enchantment.template.EnchantmentEquipEventReceiver;
import io.wispforest.affinity.misc.LivingEntityTickEvent;
import io.wispforest.affinity.misc.components.AffinityComponents;
import io.wispforest.affinity.misc.components.EntityFlagComponent;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract boolean hasStatusEffect(StatusEffect effect);

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickEnd(CallbackInfo ci) {
        LivingEntityTickEvent.EVENT.invoker().onTick((LivingEntity) (Object) this);
    }

    @Inject(method = "applyDamage", at = @At("TAIL"))
    private void applyLifeLeech(DamageSource source, float amount, CallbackInfo ci) {
        if (!(source.getAttacker() instanceof PlayerEntity player)) return;
        if (!player.hasStatusEffect(AffinityStatusEffects.LIFE_LEECH)) return;

        player.heal(amount * 0.1f * (player.getStatusEffect(AffinityStatusEffects.LIFE_LEECH).getAmplifier() + 1));
    }

    @Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true)
    private void doNotWearLeatherHats(CallbackInfoReturnable<Boolean> cir) {
        if (!this.hasStatusEffect(AffinityStatusEffects.FREEZING)) return;
        cir.setReturnValue(true);
    }

    @Inject(method = "getEquipmentChanges", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/attribute/AttributeContainer;addTemporaryModifiers(Lcom/google/common/collect/Multimap;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void onItemEquip(CallbackInfoReturnable<@Nullable Map<EquipmentSlot, ItemStack>> cir, Map<EquipmentSlot, ItemStack> map, EquipmentSlot[] var2, int var3, int var4, EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2) {
        for (var enchantment : EnchantmentHelper.get(itemStack2).keySet()) {
            if (!(enchantment instanceof EnchantmentEquipEventReceiver receiver)) continue;
            receiver.onEquip((LivingEntity) (Object) this, equipmentSlot, itemStack2);
        }
    }

    @Inject(method = "getEquipmentChanges", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/attribute/AttributeContainer;removeModifiers(Lcom/google/common/collect/Multimap;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void onItemUnequip(CallbackInfoReturnable<@Nullable Map<EquipmentSlot, ItemStack>> cir, Map<EquipmentSlot, ItemStack> map, EquipmentSlot[] var2, int var3, int var4, EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2) {
        for (var enchantment : EnchantmentHelper.get(itemStack).keySet()) {
            if (!(enchantment instanceof EnchantmentEquipEventReceiver receiver)) continue;
            receiver.onUnequip((LivingEntity) (Object) this, equipmentSlot, itemStack);
        }
    }

    @Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
    private void disableLootIfNecessary(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (AffinityComponents.ENTITY_FLAGS.get(this).hasFlag(EntityFlagComponent.NO_DROPS)) {
            ci.cancel();
        }
    }

    @Inject(method = "dropXp", at = @At("HEAD"), cancellable = true)
    private void disableXpIfNecessary(CallbackInfo ci) {
        if (AffinityComponents.ENTITY_FLAGS.get(this).hasFlag(EntityFlagComponent.NO_DROPS)) {
            ci.cancel();
        }
    }

}
