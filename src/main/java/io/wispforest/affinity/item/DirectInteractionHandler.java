package io.wispforest.affinity.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;

public interface DirectInteractionHandler {

    default boolean shouldHandleInteraction(World world, BlockPos pos, BlockState state) {
        return interactionOverrideCandidates().contains(state.getBlock());
    }

    default Collection<Block> interactionOverrideCandidates() {
        return Collections.emptyList();
    }

}
