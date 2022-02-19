package io.wispforest.affinity.entity;

import io.wispforest.affinity.object.wisps.AffinityWispTypes;
import io.wispforest.affinity.object.wisps.WispType;
import io.wispforest.affinity.object.AffinityParticleSystems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class ViciousWispEntity extends WispEntity {

    private int attackCooldown = 0;

    public ViciousWispEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
    }

    @Override
    public WispType type() {
        return AffinityWispTypes.VICIOUS;
    }

    @Override
    protected void tickServer() {
        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }

        if (this.world.getTime() % 10 != 0) return;

        var closestPlayer = this.world.getClosestPlayer(this, 10);
        if (closestPlayer == null) return;
        if (!closestPlayer.canTakeDamage()) return;

        var hitResult = this.world.raycast(new RaycastContext(this.getPos(), closestPlayer.getEyePos(),
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));

        if (hitResult.getType() != HitResult.Type.MISS) return;

        closestPlayer.damage(DamageSource.magic(this, this), 2);
        AffinityParticleSystems.WISP_ATTACK.spawn(this.world, this.getPos(),
                new AffinityParticleSystems.LineData(closestPlayer.getPos().add(0, 1, 0), this.type().color()));
        this.attackCooldown = 50;
    }

    @Override
    protected void tickClient() {

    }
}
