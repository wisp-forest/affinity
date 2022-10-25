package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.quack.AffinityChainRestrictedNeighborUpdaterExtension;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

@Mixin(ChainRestrictedNeighborUpdater.class)
public abstract class ChainRestrictedNeighborUpdaterMixin implements AffinityChainRestrictedNeighborUpdaterExtension {

    @Shadow
    @Final
    @Mutable
    private List<?> pending;

    @Shadow
    @Final
    @Mutable
    private ArrayDeque<?> queue;

    @Shadow
    protected abstract void runQueuedUpdates();

    private List<?> affinity$pending;
    private ArrayDeque<?> affinity$queue;

    @Override
    public void affinity$beginGroup() {
        this.affinity$pending = this.pending;
        this.affinity$queue = this.queue;

        this.pending = new ArrayList<>();
        this.queue = new ArrayDeque<>();
    }

    @Override
    public void affinity$submitGroup() {
        this.runQueuedUpdates();

        this.pending = this.affinity$pending;
        this.queue = this.affinity$queue;

        this.affinity$pending = null;
        this.affinity$queue = null;
    }
}
