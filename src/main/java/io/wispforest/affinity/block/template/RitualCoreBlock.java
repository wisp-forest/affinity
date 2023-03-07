package io.wispforest.affinity.block.template;

import io.wispforest.affinity.blockentity.template.RitualCoreBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class RitualCoreBlock extends AethumNetworkMemberBlock {

    protected RitualCoreBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);

        if (world.isReceivingRedstonePower(pos) && world.getBlockEntity(pos) instanceof RitualCoreBlockEntity core) {
             core.tryStartRitual();
        }
    }
}
