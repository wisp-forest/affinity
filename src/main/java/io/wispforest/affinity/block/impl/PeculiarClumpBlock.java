package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.entity.EmancipatedBlockEntity;
import io.wispforest.affinity.object.AffinityCriteria;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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

        if (player instanceof ServerPlayerEntity serverPlayer) {
            AffinityCriteria.MINED_PECULIAR_CLUMP.trigger(serverPlayer, side == validDirection);
        }

        if (side != validDirection) {
            EmancipatedBlockEntity.spawn(world, pos, state, null, 10, 0f);
        }
    }

    public static boolean getAndClearMinedState(MinecraftServer server, BlockPos pos) {
        server.execute(() -> WAS_MINED_CORRECTLY.removeBoolean(pos));
        return WAS_MINED_CORRECTLY.getBoolean(pos);
    }

    public static Direction getValidDirection(BlockPos pos) {
        RANDOM.setSeed(pos.asLong());
        return Direction.values()[RANDOM.nextInt(6)];
    }
}
