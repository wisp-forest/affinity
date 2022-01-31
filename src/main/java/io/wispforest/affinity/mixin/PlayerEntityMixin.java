package io.wispforest.affinity.mixin;

import io.wispforest.affinity.init.AffinityStatusEffects;
import io.wispforest.affinity.util.AffinityParticleSystems;
import io.wispforest.owo.ops.WorldOps;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Shadow
    public abstract void remove(RemovalReason reason);

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

}
