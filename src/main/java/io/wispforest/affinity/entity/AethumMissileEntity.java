package io.wispforest.affinity.entity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.DamageTypeKey;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinitySoundEvents;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.BuiltInEndecs;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.particles.ClientParticles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AethumMissileEntity extends ProjectileEntity {

    private static final DamageTypeKey DAMAGE_TYPE = new DamageTypeKey(Affinity.id("aethum_missile"));
    private static final KeyedEndec<UUID> TARGET_KEY = BuiltInEndecs.UUID.keyed("TargetEntity", (UUID) null);

    private UUID targetEntity = null;

    public AethumMissileEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.age > 100) {
            this.discard();
            return;
        }

        var potentialHit = ProjectileUtil.getCollision(this, this::canHit);
        if (potentialHit.getType() != HitResult.Type.MISS) this.onCollision(potentialHit);

        if (this.getWorld() instanceof ServerWorld serverWorld && this.targetEntity != null) {
            var target = serverWorld.getEntity(this.targetEntity);
            if (target == null) {
                this.discard();
                return;
            }

            this.setVelocity(MathUtil.entityCenterPos(target).subtract(MathUtil.entityCenterPos(this)).normalize().multiply(.75));
            this.velocityDirty = true;
        }

        this.updatePosition(this.getPos().x + this.getVelocity().x, this.getPos().y + this.getVelocity().y, this.getPos().z + this.getVelocity().z);

        if (this.getWorld().isClient) {
            var lastPos = new Vec3d(this.lastRenderX, this.lastRenderY, this.lastRenderZ);

            ClientParticles.setParticleCount(2);
            ClientParticles.spawnLine(ParticleTypes.WITCH, this.getWorld(), lastPos, this.getPos(), .1f);
            ClientParticles.spawn(ParticleTypes.CRIT, this.getWorld(), this.getPos(), .1f);
        }
    }

    public void setTargetEntity(@NotNull Entity target) {
        this.targetEntity = target.getUuid();
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living) {
            living.timeUntilRegen = 0;
            living.hurtTime = 0;
            living.damage(DAMAGE_TYPE.source(this, this.getOwner()), 1f);

            if (this.getOwner() instanceof ServerPlayerEntity player) {
                player.getWorld().playSound(
                    null, player.getX(), player.getY(), player.getZ(), AffinitySoundEvents.ITEM_SALVO_STAFF_HIT, SoundCategory.PLAYERS, 1f, .85f + this.getWorld().random.nextFloat() * .3f
                );
            }
        }

        this.discard();
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putIfNotNull(SerializationContext.empty(), TARGET_KEY, this.targetEntity);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.targetEntity = nbt.get(TARGET_KEY);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {}
}
