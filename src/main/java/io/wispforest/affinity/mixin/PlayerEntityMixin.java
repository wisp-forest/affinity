package io.wispforest.affinity.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.enchantment.CriticalGambleEnchantmentLogic;
import io.wispforest.affinity.item.ArtifactBladeItem;
import io.wispforest.affinity.item.LavaliereOfSafeKeepingItem;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.misc.util.ExperienceUtil;
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
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Shadow
    public float experienceProgress;
    @Shadow
    public int experienceLevel;

    @Shadow
    public abstract int getNextLevelExperience();

    @Unique
    private float affinity$lastJumpAttackDamage = 0f;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "createPlayerAttributes", at = @At("RETURN"))
    private static void injectAethumAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue()
            .add(Registries.ATTRIBUTE.getEntry(AffinityEntityAttributes.MAX_AETHUM), 15)
            .add(Registries.ATTRIBUTE.getEntry(AffinityEntityAttributes.NATURAL_AETHUM_REGEN_SPEED), 0.025);
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void removeFlightWhenDamaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.getWorld().isClient) return;

        var flightEntry = Registries.STATUS_EFFECT.getEntry(AffinityStatusEffects.FLIGHT);

        if (!this.hasStatusEffect(flightEntry)) return;
        this.removeStatusEffect(flightEntry);

        AffinityParticleSystems.FLIGHT_REMOVED.spawn(this.getWorld(), getPos());
        WorldOps.playSound(this.getWorld(), getPos(), AffinitySoundEvents.EFFECT_FLIGHT_INTERRUPTED, SoundCategory.PLAYERS, .5f, 0f);
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

        var killChanceAndLevel = EnchantmentHelper.getEffectListAndLevel(weapon, AffinityEnchantmentEffectComponents.INSTANT_KILL_CHANCE);
        if (killChanceAndLevel != null && this.random.nextFloat() < killChanceAndLevel.getFirst().getValue(killChanceAndLevel.getSecond())) {
            AffinityEntityAddon.setData(this, CriticalGambleEnchantmentLogic.ACTIVATED_AT, this.getWorld().getTime());

            // TODO this is probably wrong, investigate later
            return (damage / 3) * 2;
        }

        var critMultiplierAndLevel = EnchantmentHelper.getEffectListAndLevel(weapon, AffinityEnchantmentEffectComponents.INCREASES_CRIT_DAMAGE);
        if (critMultiplierAndLevel == null) return damage;

        return damage * ((1.5f + critMultiplierAndLevel.getFirst().getValue(critMultiplierAndLevel.getSecond())) / 1.5f);
    }

    @Inject(method = "attack", at = @At(value = "CONSTANT", args = "floatValue=1.5", shift = At.Shift.BY, by = 4))
    private void storeCritState(Entity target, CallbackInfo ci, @Local(ordinal = 2) boolean crit) {
        AffinityEntityAddon.setData(this, ArtifactBladeItem.DID_CRIT, crit);
    }

    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private DamageSource convertArtifactBladeDamage(DamageSource incoming) {
        if (!ArtifactBladeItem.isBladeWithActiveAbility(this.getWorld(), this.getMainHandStack(), 1)) return incoming;
        return ArtifactBladeItem.DAMAGE_TYPE.source(incoming.getSource(), incoming.getAttacker());
    }

    @ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 0), ordinal = 3)
    private float applyArtifactBladeJumpDamage(float damage, Entity entity) {
        if (this.fallDistance < 2 || !ArtifactBladeItem.isBladeWithActiveAbility(this.getWorld(), this.getMainHandStack(), 2)) {
            return damage;
        }

        float multiplier = Math.min(this.fallDistance * .4f, 3f);
        return this.affinity$lastJumpAttackDamage = damage * multiplier;
    }

    @ModifyVariable(method = "attack", at = @At(value = "LOAD", ordinal = 0), ordinal = 2)
    private boolean disableCrit(boolean allowCrit) {
        if (!ArtifactBladeItem.isBladeWithActiveAbility(this.getWorld(), this.getMainHandStack(), 2)) return allowCrit;
        return allowCrit && this.fallDistance < 2;
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private void artifactBladeAreaDamage(Entity target, CallbackInfo ci) {
        if (this.fallDistance < 2 || !ArtifactBladeItem.isBladeWithActiveAbility(this.getWorld(), this.getMainHandStack(), 2)) {
            return;
        }

        var entityPositions = new ArrayList<Vec3d>();

        var area = new Box(target.getBlockPos()).expand(5, 3, 5);
        for (var entity : this.getWorld().getNonSpectatingEntities(LivingEntity.class, area)) {
            if (entity == this || entity == target) continue;

            entity.damage(this.getDamageSources().playerAttack((PlayerEntity) (Object) this), this.affinity$lastJumpAttackDamage * .25f);
            entity.takeKnockback(.5, target.getX() - entity.getX(), target.getZ() - entity.getZ());

            entityPositions.add(entity.getPos());
        }

        if (!this.getWorld().isClient) {
            AffinityCriteria.ARTIFACT_BLADE_SMASH.trigger((ServerPlayerEntity) (Object) this);
            WorldOps.playSound(
                    this.getWorld(), this.getPos(),
                    AffinitySoundEvents.ITEM_ARTIFACT_BLADE_SMASH,
                    this.getSoundCategory(),
                    1, .6f + this.getWorld().random.nextFloat() * .4f
            );

            AffinityParticleSystems.ARTIFACT_BLADE_SMASH.spawn(
                    this.getWorld(),
                    target.getPos(),
                    new AffinityParticleSystems.ArtifactBladeAreaAttackData(target.getPos(), entityPositions)
            );
        }
    }

    @Inject(method = "getXpToDrop", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/player/PlayerEntity;experienceLevel:I"), cancellable = true)
    public void preserveExperience(CallbackInfoReturnable<Integer> cir) {
        if (!AffinityEntityAddon.getData(this, LavaliereOfSafeKeepingItem.IS_EQUIPPED)) return;

        var aethum = this.getComponent(AffinityComponents.PLAYER_AETHUM);
        int limit = (int) (.12 * aethum.getAethum() * aethum.getAethum() * 100);

        int levelExperience = ExperienceUtil.toPoints(this.experienceLevel);
        int total = (int) (levelExperience + this.experienceProgress * this.getNextLevelExperience());

        cir.setReturnValue(Math.min(total, limit));
    }
}
