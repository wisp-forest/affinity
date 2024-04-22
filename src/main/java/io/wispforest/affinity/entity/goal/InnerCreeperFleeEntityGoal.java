package io.wispforest.affinity.entity.goal;

import io.wispforest.affinity.component.AffinityComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.PathAwareEntity;

public class InnerCreeperFleeEntityGoal<T extends LivingEntity> extends FleeEntityGoal<T> {
    public InnerCreeperFleeEntityGoal(PathAwareEntity mob, Class<T> fleeFromType, float distance, double slowSpeed, double fastSpeed) {
        super(mob, fleeFromType, distance, slowSpeed, fastSpeed);
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
