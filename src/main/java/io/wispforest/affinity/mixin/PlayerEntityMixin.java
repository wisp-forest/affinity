package io.wispforest.affinity.mixin;

import io.wispforest.affinity.enchantment.impl.CriticalGambleEnchantment;
import io.wispforest.affinity.misc.AffinityEntityAddon;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.object.AffinityEnchantments;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinityStatusEffects;
import io.wispforest.owo.ops.WorldOps;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Shadow
    public abstract void remove(RemovalReason reason);

    @Shadow
    public abstract boolean damage(DamageSource source, float amount);

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void removeFlightWhenDamaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (world.isClient) return;

        if (!this.hasStatusEffect(AffinityStatusEffects.FLIGHT)) return;
        this.removeStatusEffect(AffinityStatusEffects.FLIGHT);

        AffinityParticleSystems.FLIGHT_REMOVED.spawn(world, getPos());
        WorldOps.playSound(world, getPos(), SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.PLAYERS, .5f, 0f);
    }

    @ModifyVariable(method = "attack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;getAttackCooldownProgress(F)F"),
            ordinal = 1)
    private float applyExtraAttackDamage(float amount, Entity entity) {
        return MixinHooks.getExtraAttackDamage(this, entity, amount);
    }

    @ModifyVariable(method = "attack",
            at = @At(value = "CONSTANT", args = "floatValue=1.5", shift = At.Shift.BY, by = 3), ordinal = 0)
    private float applyWoundingMultiplier(float damage, Entity entity) {
        final var weapon = this.getMainHandStack();

        final int criticalGambleLevel = EnchantmentHelper.getLevel(AffinityEnchantments.CRITICAL_GAMBLE, weapon);
        if (criticalGambleLevel > 0 && this.random.nextFloat() < criticalGambleLevel * .01f) {
            AffinityEntityAddon.setData(this, CriticalGambleEnchantment.ACTIVATED_AT, this.world.getTime());
            return (damage / 3) * 2;
        }

        final int woundingLevel = EnchantmentHelper.getLevel(AffinityEnchantments.WOUNDING, weapon);
        if (woundingLevel < 1) return damage;

        return damage * ((1.5f + .1f * woundingLevel) / 1.5f);
    }
}
