package io.wispforest.affinity.block.impl;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Random;

public class PeculiarClumpBlock extends Block {

    private static final Random RANDOM = new Random();
    private static final Object2BooleanMap<BlockPos> WAS_MINED_CORRECTLY = new Object2BooleanOpenHashMap<>();

    public PeculiarClumpBlock() {
        super(FabricBlockSettings.copyOf(Blocks.STONE));
        WAS_MINED_CORRECTLY.defaultReturnValue(false);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (world.isClient()) return;

        var hitResult = player.raycast(15, 1f, false);
        if (!(hitResult instanceof BlockHitResult blockResult)) return;

        final var side = blockResult.getSide();
        final var validDirection = getValidDirection(pos);
        WAS_MINED_CORRECTLY.put(pos, side == validDirection);
    }

    public static boolean getAndClearMinedState(BlockPos pos) {
        return WAS_MINED_CORRECTLY.removeBoolean(pos);
    }

    public static Direction getValidDirection(BlockPos pos) {
        RANDOM.setSeed(pos.asLong());
        return Direction.values()[RANDOM.nextInt(6)];
    }
}
