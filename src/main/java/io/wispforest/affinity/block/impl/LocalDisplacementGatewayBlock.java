package io.wispforest.affinity.block.impl;

import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.blockentity.impl.LocalDisplacementGatewayBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.object.AffinityParticleSystems;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class LocalDisplacementGatewayBlock extends BlockWithEntity {

    public static final VoxelShape SHAPE = Stream.of(
            Block.createCuboidShape(13, 0, 3, 16, 3, 13),
            Block.createCuboidShape(0, 0, 0, 3, 16, 3),
            Block.createCuboidShape(13, 0, 0, 16, 16, 3),
            Block.createCuboidShape(0, 0, 13, 3, 16, 16),
            Block.createCuboidShape(13, 0, 13, 16, 16, 16),
            Block.createCuboidShape(0, 0, 3, 3, 3, 13),
            Block.createCuboidShape(3, 0, 0, 13, 3, 3),
            Block.createCuboidShape(3, 0, 13, 13, 3, 16),
            Block.createCuboidShape(0, 13, 3, 3, 16, 13),
            Block.createCuboidShape(3, 13, 0, 13, 16, 3),
            Block.createCuboidShape(13, 13, 3, 16, 16, 13),
            Block.createCuboidShape(3, 13, 13, 13, 16, 16),
            Block.createCuboidShape(3, 3, 3, 13, 13, 13)
    ).reduce(VoxelShapes::union).get();

    public LocalDisplacementGatewayBlock() {
        super(FabricBlockSettings.copyOf(Blocks.OBSIDIAN));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var playerHeight = player.getHeight();

        BlockPos targetPos;
        if (hit.getSide() == Direction.UP) {
            var basePos = pos.getY() - playerHeight;
            targetPos = BlockPos.ofFloored(pos.getX(), basePos, pos.getZ());
        } else {
            targetPos = pos.offset(hit.getSide().getOpposite());
        }

        if (!world.isAir(targetPos) || !world.isAir(BlockPos.ofFloored(targetPos.getX(), targetPos.getY() + playerHeight, targetPos.getZ()))) {
            return ActionResult.PASS;
        }

        var aethum = player.getComponent(AffinityComponents.PLAYER_AETHUM);
        if (!aethum.tryConsumeAethum(1.5f)) return ActionResult.PASS;

        player.teleport(targetPos.getX() + .5, targetPos.getY(), targetPos.getZ() + .5);

        if (!world.isClient) {
            AffinityParticleSystems.LOCAL_DISPLACEMENT_GATEWAY_TELEPORT.spawn(world, player.getPos());
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LocalDisplacementGatewayBlockEntity(pos, state);
    }
}
