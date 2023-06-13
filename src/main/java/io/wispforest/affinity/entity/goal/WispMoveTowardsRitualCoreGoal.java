package io.wispforest.affinity.entity.goal;

import io.wispforest.affinity.entity.WispEntity;
import io.wispforest.affinity.misc.util.BlockFinder;
import io.wispforest.affinity.object.AffinityPoiTypes;
import io.wispforest.owo.util.VectorRandomUtils;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterest;

import java.util.Comparator;
import java.util.EnumSet;

public class WispMoveTowardsRitualCoreGoal extends Goal {

    private final WispEntity wisp;
    private BlockPos closestCore = null;

    public WispMoveTowardsRitualCoreGoal(WispEntity wisp) {
        this.wisp = wisp;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        this.closestCore = BlockFinder.findPoi(this.wisp.getWorld(), AffinityPoiTypes.RITUAL_CORE, this.wisp.getBlockPos(), 10)
                .sorted(Comparator.comparing(poi -> poi.getPos().getSquaredDistanceFromCenter(this.wisp.getX(), this.wisp.getY(), this.wisp.getZ())))
                .map(PointOfInterest::getPos)
                .findFirst()
                .orElse(null);

        if (this.closestCore != null) {
            var closeWisps = this.wisp.getWorld().getEntitiesByClass(WispEntity.class, new Box(this.closestCore).expand(7), wispEntity -> wispEntity.type() == this.wisp.type());

            if (closeWisps.size() > 4) {
                return false;
            }
        }

        return this.closestCore != null && this.closestCore.getSquaredDistanceFromCenter(this.wisp.getX(), this.wisp.getY(), this.wisp.getZ()) >= (6 * 6);
    }

    @Override
    public void start() {
        var target = VectorRandomUtils.getRandomOffsetSpecific(this.wisp.getWorld(), Vec3d.ofCenter(this.closestCore.up(3)), 8, 5, 8);
        this.wisp.getNavigation().startMovingTo(target.x, target.y, target.z, .75);
    }

    @Override
    public boolean shouldContinue() {
        return !this.wisp.getNavigation().isIdle();
    }
}
