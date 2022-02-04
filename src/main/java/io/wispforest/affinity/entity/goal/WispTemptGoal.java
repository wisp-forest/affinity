package io.wispforest.affinity.entity.goal;

import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.recipe.Ingredient;

public class WispTemptGoal extends TemptGoal {

    private final double speed;

    public WispTemptGoal(PathAwareEntity entity, double speed, Ingredient food, boolean canBeScared) {
        super(entity, speed, food, canBeScared);
        this.speed = speed;
    }

    @Override
    public void tick() {
        this.mob.getLookControl().lookAt(this.closestPlayer, (float) (this.mob.getMaxHeadRotation() + 20), (float) this.mob.getMaxLookPitchChange());
        if (this.mob.squaredDistanceTo(this.closestPlayer) < 6.25) {
            this.mob.setVelocity(this.mob.getVelocity().multiply(.85));
            this.mob.getNavigation().stop();
        } else {
            final var target = this.closestPlayer.getPos().add(0, 1.5, 0);
            this.mob.getNavigation().startMovingTo(target.x, target.y, target.z, speed);
        }
    }
}
