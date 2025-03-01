package io.wispforest.affinity.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;

public interface DirectInteractionHandler {

    default boolean shouldHandleInteraction(ItemStack stack, World world, BlockPos pos, BlockState state) {
        return interactionOverrideCandidates(world).contains(state.getBlock());
    }

    default Collection<Block> interactionOverrideCandidates(World world) {
        return Collections.emptyList();
    }

}
