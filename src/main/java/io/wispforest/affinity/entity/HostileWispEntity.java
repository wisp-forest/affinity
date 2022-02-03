package io.wispforest.affinity.entity;

import io.wispforest.affinity.misc.AffinityParticleSystems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class HostileWispEntity extends WispEntity {

    private int attackCooldown = 0;

    public HostileWispEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
    }

    @Override
    protected int particleColor() {
        return 0xB8405E;
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
                new AffinityParticleSystems.LineData(closestPlayer.getPos().add(0, 1, 0), this.particleColor()));
        this.attackCooldown = 50;
    }

    @Override
    protected void tickClient() {

    }
}
