package io.wispforest.affinity.mixin;

import io.wispforest.affinity.enchantment.impl.CriticalGambleEnchantment;
import io.wispforest.affinity.item.ArtifactBladeItem;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.object.*;
import io.wispforest.owo.ops.WorldOps;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Shadow
    public abstract void remove(RemovalReason reason);

    @Shadow
    public abstract boolean damage(DamageSource source, float amount);

    @Shadow
    public abstract void playSound(SoundEvent sound, float volume, float pitch);

    @Shadow
    public abstract SoundCategory getSoundCategory();

    @Unique
    private float affinity$lastJumpAttackDamage = 0f;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "createPlayerAttributes", at = @At("RETURN"))
    private static void injectAethumAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue().add(AffinityEntityAttributes.MAX_AETHUM, 15).add(AffinityEntityAttributes.NATURAL_AETHUM_REGEN_SPEED, 0.025);
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
        float baseAttackDamage = (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        return amount + MixinHooks.getExtraAttackDamage(this, entity, baseAttackDamage) - baseAttackDamage;
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

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "attack",
            at = @At(value = "CONSTANT", args = "floatValue=1.5", shift = At.Shift.BY, by = 4), locals = LocalCapture.CAPTURE_FAILHARD)
    private void storeCritState(Entity target, CallbackInfo ci, float f, float g, boolean bl, boolean bl2, int i, boolean crit) {
        AffinityEntityAddon.setData(this, ArtifactBladeItem.DID_CRIT, crit);
    }

    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private DamageSource convertArtifactBladeDamage(DamageSource incoming) {
        if (!ArtifactBladeItem.isBladeWithActiveAbility(this.world, this.getMainHandStack(), 1)) return incoming;
        return ArtifactBladeItem.DAMAGE_TYPE.source(incoming.getSource(), incoming.getAttacker());
    }

    @ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 3), ordinal = 0)
    private float applyArtifactBladeJumpDamage(float damage, Entity entity) {
        if (this.fallDistance < 2 || !ArtifactBladeItem.isBladeWithActiveAbility(this.world, this.getMainHandStack(), 2))
            return damage;

        float multiplier = Math.min(this.fallDistance * .4f, 3f);
        return this.affinity$lastJumpAttackDamage = damage * multiplier;
    }

    @ModifyVariable(method = "attack", at = @At(value = "LOAD", ordinal = 0), ordinal = 2)
    private boolean disableCrit(boolean allowCrit) {
        if (!ArtifactBladeItem.isBladeWithActiveAbility(this.world, this.getMainHandStack(), 2)) return allowCrit;
        return allowCrit && this.fallDistance < 2;
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private void artifactBladeAreaDamage(Entity target, CallbackInfo ci) {
        if (this.fallDistance < 2 || !ArtifactBladeItem.isBladeWithActiveAbility(this.world, this.getMainHandStack(), 2))
            return;

        var entityPositions = new ArrayList<Vec3d>();

        var area = new Box(target.getBlockPos()).expand(5, 3, 5);
        for (var entity : this.world.getNonSpectatingEntities(LivingEntity.class, area)) {
            if (entity == this || entity == target) continue;

            entity.damage(this.getDamageSources().playerAttack((PlayerEntity) (Object) this), this.affinity$lastJumpAttackDamage * .25f);
            entity.takeKnockback(.5, target.getX() - entity.getX(), target.getZ() - entity.getZ());

            entityPositions.add(entity.getPos());
        }

        if (!this.world.isClient) {
            AffinityCriteria.ARTIFACT_BLADE_SMASH.trigger((ServerPlayerEntity) (Object) this);
            WorldOps.playSound(
                    this.world, this.getPos(),
                    AffinitySoundEvents.ITEM_ARTIFACT_BLADE_SMASH,
                    this.getSoundCategory(),
                    1, .6f + this.world.random.nextFloat() * .4f
            );

            AffinityParticleSystems.ARTIFACT_BLADE_SMASH.spawn(
                    this.world,
                    target.getPos(),
                    new AffinityParticleSystems.ArtifactBladeAreaAttackData(target.getPos(), entityPositions)
            );
        }
    }
}
