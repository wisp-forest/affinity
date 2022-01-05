package io.wispforest.affinity.entity;

import io.wispforest.affinity.util.MathUtil;
import io.wispforest.owo.particles.ClientParticles;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Random;

public class WispEntity extends PathAwareEntity {

    private static final DustParticleEffect PARTICLES = new DustParticleEffect(MathUtil.splitRGBToVector(0x6A8CAF), 1);

    public WispEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new FlightMoveControl(this, 75, true);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new MoveRandomlyGoal(this));
    }

    public static DefaultAttributeContainer.Builder createWispAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 5)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, .25)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, .6);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {}

    @Override
    public void tick() {
        super.tick();
        if (!world.isClient) return;
        ClientParticles.spawnPrecise(PARTICLES, world, this.getPos(), .2, .2, .2);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new Navigation(this, world);
    }

    public static boolean isValidSpawn(EntityType<WispEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos).isAir() && world.getLightLevel(pos) > 5;
    }

    private static class Navigation extends BirdNavigation {

        public Navigation(MobEntity mobEntity, World world) {
            super(mobEntity, world);
        }

        @Override
        public boolean isValidPosition(BlockPos pos) {
            return true;
        }
    }

    private static class MoveRandomlyGoal extends Goal {

        private final WispEntity entity;
        private Vec3d target;

        private MoveRandomlyGoal(WispEntity entity) {
            this.entity = entity;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            var pos = getTarget();
            if (pos == null) return false;

            this.target = pos;
            return true;
        }

        @Override
        public void start() {
            this.entity.navigation.startMovingTo(target.x, target.y, target.z, .6);
        }

        @Override
        public boolean shouldContinue() {
            return !this.entity.getNavigation().isIdle();
        }

        @Override
        public void stop() {
            this.entity.getNavigation().stop();
            super.stop();
        }

        @Nullable
        protected Vec3d getTarget() {
            return NoPenaltyTargeting.find(this.entity, 10, 10);
        }
    }
}
