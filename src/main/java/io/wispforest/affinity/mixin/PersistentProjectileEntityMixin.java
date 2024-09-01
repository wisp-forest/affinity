package io.wispforest.affinity.mixin;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityEntityAttributes;
import io.wispforest.affinity.particle.ColoredFallingDustParticleEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin extends Entity {

    public PersistentProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyArgs(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V", ordinal = 0))
    private void injectAzaleaBowParticles(Args args) {
        if (!this.getComponent(AffinityComponents.ENTITY_FLAGS).hasFlag(EntityFlagComponent.SHOT_BY_AZALEA_BOW)) return;
        if (this.age < 2) return;

        this.getWorld().addParticle(ParticleTypes.FIREWORK, args.<Double>get(1), args.<Double>get(2), args.<Double>get(3), 0, 0, 0);
        args.set(0, new ColoredFallingDustParticleEffect(MathUtil.rgbToVec3f(Affinity.AETHUM_FLUX_COLOR.rgb())));
    }

    @Inject(method = "setOwner", at = @At("TAIL"))
    private void setExtraDamage(Entity owner, CallbackInfo ci) {
        if (!(owner instanceof LivingEntity living)) return;

        var damageComponent = this.getComponent(AffinityComponents.EXTRA_ARROW_DAMAGE);
        damageComponent.extraDamage = (int) living.getAttributeValue(AffinityEntityAttributes.EXTRA_ARROW_DAMAGE);
    }

    @ModifyVariable(method = "onEntityHit", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private int applyExtraDamage(int damage) {
        return damage + this.getComponent(AffinityComponents.EXTRA_ARROW_DAMAGE).extraDamage;
    }

}
