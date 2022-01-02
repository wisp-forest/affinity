package io.wispforest.affinity.block.impl;

import io.wispforest.owo.ops.WorldOps;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class UnfloweringAzaleaLeavesBlock extends LeavesBlock {

    private static final int MAX_AGE = 2;
    private static final IntProperty AGE = Properties.AGE_2;

    public UnfloweringAzaleaLeavesBlock() {
        super(FabricBlockSettings.copyOf(Blocks.FLOWERING_AZALEA_LEAVES));
        setDefaultState(getDefaultState().with(AGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(AGE);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.isSneaking()) return ActionResult.PASS;

        if (!world.isClient) {
            for (var testPos : BlockPos.iterate(pos.add(-16, -3, -16), pos.add(16, 3, 16))) {
                if (!(world.getBlockState(testPos).getBlock() instanceof PlantBlock)) continue;
                WorldOps.breakBlockWithItem(world, testPos, ItemStack.EMPTY);
            }
            WorldOps.breakBlockWithItem(world, pos, ItemStack.EMPTY);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int age = state.get(AGE);
        if (age < MAX_AGE) world.setBlockState(pos, state.with(AGE, age + 1));

        if (age == MAX_AGE) {
            world.setBlockState(pos, Blocks.FLOWERING_AZALEA_LEAVES.getDefaultState()
                    .with(LeavesBlock.PERSISTENT, state.get(LeavesBlock.PERSISTENT)));
            return;
        }

        if (super.hasRandomTicks(state)) super.randomTick(state, world, pos, random);
    }
}
