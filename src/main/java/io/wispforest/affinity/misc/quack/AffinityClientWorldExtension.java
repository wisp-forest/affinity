package io.wispforest.affinity.misc.quack;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface AffinityClientWorldExtension {
    void affinity$addBlockUpdateListener(BlockUpdateListener listener);

    @FunctionalInterface
    interface BlockUpdateListener {
        boolean onBlockUpdate(BlockPos pos, BlockState from, BlockState to);
    }
}
