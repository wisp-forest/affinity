package io.wispforest.affinity.entity.goal;

import io.wispforest.affinity.entity.WispEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class FlyRandomlyGoal extends Goal {

    private final WispEntity wisp;
    private Vec3d target;

    public FlyRandomlyGoal(WispEntity wisp) {
        this.wisp = wisp;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        var pos = this.getTarget();
        if (pos == null) return false;

        this.target = pos;
        return true;
    }

    @Override
    public void start() {
        this.wisp.getNavigation().startMovingTo(target.x, target.y, target.z, .6);
    }

    @Override
    public boolean shouldContinue() {
        return !this.wisp.getNavigation().isIdle();
    }

    @Override
    public void stop() {
        this.wisp.getNavigation().stop();
        super.stop();
    }

    protected @Nullable Vec3d getTarget() {
        return NoPenaltyTargeting.find(this.wisp, 10, 10);
    }
}
