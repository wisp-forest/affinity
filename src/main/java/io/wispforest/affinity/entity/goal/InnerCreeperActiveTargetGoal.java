package io.wispforest.affinity.entity.goal;

import io.wispforest.affinity.component.AffinityComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;

public class InnerCreeperActiveTargetGoal<T extends LivingEntity> extends ActiveTargetGoal<T> {
    public InnerCreeperActiveTargetGoal(MobEntity mob, Class<T> targetClass, boolean checkVisibility) {
        super(mob, targetClass, checkVisibility);
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
