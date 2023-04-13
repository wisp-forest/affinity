package io.wispforest.affinity.entity.goal;

import io.wispforest.affinity.entity.WispEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;

public class WispFleeFromPlayerGoal extends Goal {

    private final WispEntity wisp;
    private final BooleanSupplier isScared;

    private Path fleePath = null;

    public WispFleeFromPlayerGoal(WispEntity wisp, BooleanSupplier isScared) {
        this.wisp = wisp;
        this.isScared = isScared;

        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        var scared = this.isScared.getAsBoolean();
        if (!scared) return false;

        PlayerEntity closestPlayer = this.wisp.world.getClosestPlayer(this.wisp, 20);
        if (closestPlayer == null) return false;

        var fleePos = NoPenaltyTargeting.findFrom(this.wisp, 10, 10, this.wisp.getPos().add(0, 5, 0));
        if (fleePos == null || closestPlayer.squaredDistanceTo(fleePos) < closestPlayer.squaredDistanceTo(this.wisp))
            return false;

        this.fleePath = this.wisp.getNavigation().findPathTo(fleePos.x, fleePos.y, fleePos.z, 0);
        return true;
    }

    @Override
    public void start() {
        this.wisp.getNavigation().startMovingAlong(this.fleePath, 5);
    }

    @Override
    public boolean shouldContinue() {
        return !this.wisp.getNavigation().isIdle();
    }
}
