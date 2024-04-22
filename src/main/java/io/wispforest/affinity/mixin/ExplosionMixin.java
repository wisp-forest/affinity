package io.wispforest.affinity.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.misc.quack.AffinityExplosionExtension;
import net.minecraft.entity.Entity;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public class ExplosionMixin implements AffinityExplosionExtension {

    @Unique
    private boolean noEntityDrops = false;

    @Override
    public void affinity$markNoEntityDrops() {
        this.noEntityDrops = true;
    }

    @Inject(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private void disableDrops(CallbackInfo ci, @Local Entity entity) {
        if (!this.noEntityDrops) return;
        entity.getComponent(AffinityComponents.ENTITY_FLAGS).setFlag(EntityFlagComponent.NO_DROPS);
    }

    @Inject(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", shift = At.Shift.AFTER))
    private void enableDrops(CallbackInfo ci, @Local Entity entity) {
        if (!this.noEntityDrops || !entity.isAlive()) return;
        entity.getComponent(AffinityComponents.ENTITY_FLAGS).unsetFlag(EntityFlagComponent.NO_DROPS);
    }
}
