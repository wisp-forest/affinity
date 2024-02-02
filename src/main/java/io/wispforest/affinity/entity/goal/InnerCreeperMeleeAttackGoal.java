package io.wispforest.affinity.entity.goal;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.object.AffinityStatusEffects;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;

public class InnerCreeperMeleeAttackGoal extends MeleeAttackGoal {
    public InnerCreeperMeleeAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle) {
        super(mob, speed, pauseWhenMobIdle);
    }

    @Override
    public boolean canStart() {
        return this.mob.getComponent(AffinityComponents.INNER_CREEPER).active() && super.canStart();
    }

    @Override
    public boolean shouldContinue() {
        return this.mob.getComponent(AffinityComponents.INNER_CREEPER).active() && super.shouldContinue();
    }
}
