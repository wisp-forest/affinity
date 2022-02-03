package io.wispforest.affinity.entity;

import io.wispforest.affinity.entity.goal.FlyRandomlyGoal;
import io.wispforest.affinity.util.MathUtil;
import io.wispforest.owo.particles.ClientParticles;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

import java.util.Random;

public abstract class WispEntity extends PathAwareEntity {

    private final DustParticleEffect particles;

    public WispEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);

        this.particles = new DustParticleEffect(MathUtil.splitRGBToVector(this.particleColor()), 1);
        this.moveControl = new FlightMoveControl(this, 75, true);
    }

    protected abstract void tickServer();

    protected abstract void tickClient();

    protected abstract int particleColor();

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new FlyRandomlyGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isClient) {
            this.tickServer();
        } else {
            this.tickClient();
            ClientParticles.spawnPrecise(particles, world, this.getPos(), .2, .2, .2);
        }
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {}

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new BirdNavigation(this, world) {
            @Override
            public boolean isValidPosition(BlockPos pos) {
                return true;
            }
        };
    }

    public static DefaultAttributeContainer.Builder createWispAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 5)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, .25)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, .6);
    }

    public static <E extends WispEntity> boolean isValidSpawn(EntityType<E> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos).isAir() && world.getLightLevel(pos) > 5;
    }
}
