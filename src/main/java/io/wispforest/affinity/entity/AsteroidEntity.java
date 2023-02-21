package io.wispforest.affinity.entity;

import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.particles.ClientParticles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AsteroidEntity extends ProjectileEntity {

    protected static final NbtKey<Float> EXPLOSION_POWER = new NbtKey<>("ExplosionPower", NbtKey.Type.FLOAT);

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

        if (this.world.isClient && this.getVelocity().lengthSquared() > 0) {
            var lastPos = new Vec3d(this.lastRenderX, this.lastRenderY, this.lastRenderZ);

            if (this.getVelocity().lengthSquared() > .5) {
                ClientParticles.setParticleCount(5);
                ClientParticles.spawnLine(ParticleTypes.EXPLOSION, this.world, lastPos, this.getPos(), 1.5f);

                ClientParticles.setParticleCount(5);
                ClientParticles.spawnLine(ParticleTypes.POOF, this.world, lastPos, this.getPos(), 1f);
            } else {
                ClientParticles.setParticleCount(2);
                ClientParticles.spawnLine(ParticleTypes.SMOKE, this.world, lastPos, this.getPos(), .75f);
            }
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (hitResult.getType() == HitResult.Type.MISS) return;

        this.world.createExplosion(this, new AsteroidDamageSource(this, this.getOwner()), null, hitResult.getPos(), this.explosionPower, true, World.ExplosionSourceType.TNT);
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
    protected void initDataTracker() {}

    public static class AsteroidDamageSource extends EntityDamageSource {

        private final @Nullable Entity owner;

        public AsteroidDamageSource(Entity entity, @Nullable Entity owner) {
            super("asteroid", entity);
            this.owner = owner;
        }

        @Override
        public Text getDeathMessage(LivingEntity entity) {
            return this.owner == null
                    ? Text.translatable("death.attack." + this.name, entity.getDisplayName())
                    : Text.translatable("death.attack." + this.name + ".player", entity.getDisplayName(), this.owner.getDisplayName());
        }
    }
}
