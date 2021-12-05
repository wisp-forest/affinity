package io.wispforest.affinity.block;

import io.wispforest.affinity.blockentity.AetherNetworkMemberBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AetherNetworkMemberBlock extends BlockWithEntity {

    protected AetherNetworkMemberBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {

            if (world.getBlockEntity(pos) instanceof AetherNetworkMemberBlockEntity member) {
                member.onBroken();
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }
}
