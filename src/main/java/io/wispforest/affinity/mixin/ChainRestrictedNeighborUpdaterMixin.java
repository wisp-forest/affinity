package io.wispforest.affinity.mixin;

import io.wispforest.affinity.misc.quack.AffinityChainRestrictedNeighborUpdaterExtension;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import org.spongepowered.asm.mixin.*;

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

    @Shadow private int depth;

    @Unique private final ArrayDeque<List<?>> affinity$pending = new ArrayDeque<>();
    @Unique private final ArrayDeque<ArrayDeque<?>> affinity$queue = new ArrayDeque<>();

    @Override
    public void affinity$beginGroup() {
        this.affinity$pending.push(this.pending);
        this.affinity$queue.push(this.queue);

        this.pending = new ArrayList<>();
        this.queue = new ArrayDeque<>();
    }

    @Override
    public void affinity$submitGroup() {
        var depth = this.depth;
        this.runQueuedUpdates();
        this.depth = depth;

        this.pending = this.affinity$pending.pop();
        this.queue = this.affinity$queue.pop();
    }
}
