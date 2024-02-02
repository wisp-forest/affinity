package io.wispforest.affinity.entity.goal;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class InnerCreeperExplodeGoal extends Goal {

    private final PathAwareEntity entity;
    private @Nullable LivingEntity target;

    public InnerCreeperExplodeGoal(PathAwareEntity creeper) {
        this.entity = creeper;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (!this.entity.getComponent(AffinityComponents.INNER_CREEPER).active()) return false;

        var entityTarget = this.entity.getTarget();
        return this.entity.getComponent(AffinityComponents.INNER_CREEPER).fuseDirection() > 0 || entityTarget != null && this.entity.squaredDistanceTo(entityTarget) < 9.0;
    }

    @Override
    public void start() {
        this.target = this.entity.getTarget();
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.entity.getNavigation().stop();
        var creeper = this.entity.getComponent(AffinityComponents.INNER_CREEPER);

        if (this.target == null) {
            creeper.fuseDirection(-1);
        } else if (this.entity.squaredDistanceTo(this.target) > 49.0) {
            creeper.fuseDirection(-1);
        } else if (!this.entity.getVisibilityCache().canSee(this.target)) {
            creeper.fuseDirection(-1);
        } else {
            creeper.fuseDirection(1);
        }
    }

}
