package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.blockentity.impl.RitualSocleBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class RitualSocleBlock extends BlockWithEntity {

    public static final VoxelShape SHAPE = Stream.of(
            BlankRitualSocleBlock.SHAPE,
            Block.createCuboidShape(4, 8, 4, 12, 11, 5),
            Block.createCuboidShape(11, 8, 5, 12, 11, 11),
            Block.createCuboidShape(4, 8, 5, 5, 11, 11),
            Block.createCuboidShape(4, 8, 11, 12, 11, 12)
    ).reduce(VoxelShapes::union).get();

    private final RitualSocleType type;

    public RitualSocleBlock(RitualSocleType type) {
        super(FabricBlockSettings.copyOf(Blocks.SMOOTH_STONE).nonOpaque().solid());
        this.type = type;
    }

    public int glowColor() {
        return this.type.glowColor();
    }

    public RitualSocleType type() {
        return this.type;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return InteractableBlockEntity.tryHandle(world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, AffinityBlocks.Entities.RITUAL_SOCLE, TickedBlockEntity.ticker());
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof RitualSocleBlockEntity socle) {
                socle.onBroken();
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RitualSocleBlockEntity(pos, state);
    }

}
