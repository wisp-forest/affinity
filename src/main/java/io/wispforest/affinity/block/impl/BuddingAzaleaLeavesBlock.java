package io.wispforest.affinity.block.impl;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class BuddingAzaleaLeavesBlock extends LeavesBlock {

    private static final int MAX_AGE = 2;
    private static final IntProperty AGE = Properties.AGE_2;

    public BuddingAzaleaLeavesBlock() {
        super(FabricBlockSettings.copyOf(Blocks.FLOWERING_AZALEA_LEAVES));
        setDefaultState(getDefaultState().with(AGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(AGE);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (random.nextFloat() > .75f) {
            int age = state.get(AGE);
            if (age < MAX_AGE) world.setBlockState(pos, state.with(AGE, age + 1));

            if (age == MAX_AGE) {
                world.setBlockState(pos, Blocks.FLOWERING_AZALEA_LEAVES.getDefaultState()
                        .with(LeavesBlock.PERSISTENT, state.get(LeavesBlock.PERSISTENT)));
                return;
            }
        }

        if (super.hasRandomTicks(state)) super.randomTick(state, world, pos, random);
    }
}
