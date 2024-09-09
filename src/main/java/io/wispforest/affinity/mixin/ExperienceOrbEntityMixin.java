package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.quack.AffinityExperienceOrbExtension;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityPoiTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin extends Entity implements AffinityExperienceOrbExtension {

    @Shadow private PlayerEntity target;

    @Unique
    private @Nullable BlockPos armatureTargetPos = null;

    public ExperienceOrbEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "expensiveUpdate", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getClosestPlayer(Lnet/minecraft/entity/Entity;D)Lnet/minecraft/entity/player/PlayerEntity;"))
    private void injectArmaturePOIs(CallbackInfo ci) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        var nearestPoi = serverWorld.getPointOfInterestStorage().getNearestPosition(
                entry -> entry.value() == AffinityPoiTypes.VILLAGER_ARMATURE,
                this.getBlockPos(),
                8, PointOfInterestStorage.OccupationStatus.ANY
        );

        var poiPos = nearestPoi.orElse(null);
        if (poiPos != null && this.target != null && this.squaredDistanceTo(Vec3d.ofCenter(poiPos)) > this.squaredDistanceTo(this.target)) {
            poiPos = null;
        }

        if (!Objects.equals(poiPos, this.armatureTargetPos)) {
            this.armatureTargetPos = poiPos;
            AffinityNetwork.server(serverWorld, this.getBlockPos()).send(new AffinityNetwork.SetExperienceOrbTargetPacket(this.getId(), this.armatureTargetPos));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void cancelPlayerTargeting(CallbackInfo ci) {
        if (this.armatureTargetPos != null) this.target = null;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"))
    private void injectArmatureTargeting(CallbackInfo ci) {
        if (this.armatureTargetPos == null) return;

        var targetPos = Vec3d.ofCenter(this.armatureTargetPos);
        var offset = new Vec3d(targetPos.getX() - this.getX(), targetPos.getY() - this.getY(), targetPos.getZ() - this.getZ());

        double offsetLengthSquared = offset.lengthSquared();
        if (offsetLengthSquared < 64.0) {
            double pullStrength = 1.0 - Math.sqrt(offsetLengthSquared) / 8.0;
            this.setVelocity(this.getVelocity().add(offset.normalize().multiply(pullStrength * pullStrength * 0.1)));
        }
    }

    @Override
    public void affinity$setArmatureTarget(BlockPos pos) {
        this.armatureTargetPos = pos;
    }
}
