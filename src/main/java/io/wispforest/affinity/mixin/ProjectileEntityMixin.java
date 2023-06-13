package io.wispforest.affinity.mixin;

import io.wispforest.affinity.item.KinesisStaffItem;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.object.AffinityCriteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity {

    public ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V"))
    private void triggerKinesisCriterion(HitResult hitResult, CallbackInfo ci) {
        if (!AffinityEntityAddon.hasData(this, KinesisStaffItem.PROJECTILE_THROWER)) return;

        var throwerId = AffinityEntityAddon.getData(this, KinesisStaffItem.PROJECTILE_THROWER);
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        var thrower = serverWorld.getEntity(throwerId);
        if (!(thrower instanceof ServerPlayerEntity player)) return;

        AffinityCriteria.KINESIS_PROJECTILE_HIT.trigger(player, ((EntityHitResult) hitResult).getEntity(), this);
    }

}
