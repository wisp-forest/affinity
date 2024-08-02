package io.wispforest.affinity.entity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.DamageTypeKey;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.particles.ClientParticles;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AsteroidEntity extends ProjectileEntity {

    public static final DamageTypeKey ASTEROID_DAMAGE = new DamageTypeKey(Affinity.id("asteroid"));
    protected static final KeyedEndec<Float> EXPLOSION_POWER = Endec.FLOAT.keyed("ExplosionPower", 0f);

    protected float explosionPower = 0f;

    public AsteroidEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();

        var potentialHit = ProjectileUtil.getCollision(this, this::canHit);
        this.onCollision(potentialHit);

        this.updatePosition(this.getPos().x + this.getVelocity().x, this.getPos().y + this.getVelocity().y, this.getPos().z + this.getVelocity().z);

        if (this.getWorld().isClient && this.getVelocity().lengthSquared() > 0) {
            var lastPos = new Vec3d(this.lastRenderX, this.lastRenderY, this.lastRenderZ);

            if (this.getVelocity().lengthSquared() > .5) {
                ClientParticles.setParticleCount(5);
                ClientParticles.spawnLine(ParticleTypes.EXPLOSION, this.getWorld(), lastPos, this.getPos(), 1.5f);

                ClientParticles.setParticleCount(5);
                ClientParticles.spawnLine(ParticleTypes.POOF, this.getWorld(), lastPos, this.getPos(), 1f);
            } else {
                ClientParticles.setParticleCount(2);
                ClientParticles.spawnLine(ParticleTypes.SMOKE, this.getWorld(), lastPos, this.getPos(), .75f);
            }
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (hitResult.getType() == HitResult.Type.MISS) return;

        this.getWorld().createExplosion(this, ASTEROID_DAMAGE.source(this, this.getOwner()), null, hitResult.getPos(), this.explosionPower, true, World.ExplosionSourceType.TNT);
        this.discard();
    }

    public void setExplosionPower(float explosionPower) {
        this.explosionPower = explosionPower;
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.put(EXPLOSION_POWER, this.explosionPower);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.explosionPower = nbt.get(EXPLOSION_POWER);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {}
}
