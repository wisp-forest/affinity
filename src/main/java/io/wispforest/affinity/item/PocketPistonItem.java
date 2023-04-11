package io.wispforest.affinity.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;
import java.util.Map;

import static net.minecraft.block.Block.dropStacks;

public class PocketPistonItem extends Item {
    public PocketPistonItem() {
        super(AffinityItems.settings(AffinityItemGroup.EQUIPMENT).maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        boolean push = !context.getPlayer().isSneaking();

        if (move(
            context.getWorld(),
            context.getBlockPos().offset(context.getSide(), push ? 1 : 2),
            context.getSide().getOpposite(),
            push
        )) {
            context.getWorld().playSound(
                context.getPlayer(),
                context.getPlayer().getBlockPos(),
                push ? SoundEvents.BLOCK_PISTON_EXTEND : SoundEvents.BLOCK_PISTON_CONTRACT,
                SoundCategory.BLOCKS,
                0.5F,
                context.getWorld().random.nextFloat() * 0.25F + 0.6F
            );

            return ActionResult.success(context.getWorld().isClient);
        } else {
            return ActionResult.PASS;
        }
    }

    // Mostly a vanilla copy of PistonBlock#move
    private static boolean move(World world, BlockPos pos, Direction dir, boolean retract) {
        PistonHandler pistonHandler = new PistonHandler(world, pos, dir, retract);
        if (!pistonHandler.calculatePush()) {
            return false;
        } else {
            Map<BlockPos, BlockState> movedBlocks = Maps.newHashMap();
            List<BlockState> list2 = Lists.newArrayList();

            for (BlockPos blockPos2 : pistonHandler.getMovedBlocks()) {
                BlockState blockState = world.getBlockState(blockPos2);
                list2.add(blockState);
                movedBlocks.put(blockPos2, blockState);
            }

            BlockState[] blockStates = new BlockState[pistonHandler.getMovedBlocks().size() + pistonHandler.getBrokenBlocks().size()];
            Direction direction = retract ? dir : dir.getOpposite();
            int j = 0;

            for(int k = pistonHandler.getBrokenBlocks().size() - 1; k >= 0; --k) {
                BlockPos blockPos3 = pistonHandler.getBrokenBlocks().get(k);
                BlockState blockState2 = world.getBlockState(blockPos3);
                BlockEntity blockEntity = blockState2.hasBlockEntity() ? world.getBlockEntity(blockPos3) : null;
                dropStacks(blockState2, world, blockPos3, blockEntity);
                world.setBlockState(blockPos3, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
                world.emitGameEvent(GameEvent.BLOCK_DESTROY, blockPos3, GameEvent.Emitter.of(blockState2));
                if (!blockState2.isIn(BlockTags.FIRE)) {
                    world.addBlockBreakParticles(blockPos3, blockState2);
                }

                blockStates[j++] = blockState2;
            }

            for(int k = pistonHandler.getMovedBlocks().size() - 1; k >= 0; --k) {
                BlockPos blockPos3 = pistonHandler.getMovedBlocks().get(k);
                BlockState blockState2 = world.getBlockState(blockPos3);
                blockPos3 = blockPos3.offset(direction);
                movedBlocks.remove(blockPos3);
                BlockState blockState3 = Blocks.MOVING_PISTON.getDefaultState().with(Properties.FACING, dir);
                world.setBlockState(blockPos3, blockState3, Block.NO_REDRAW | Block.MOVED);
                world.addBlockEntity(PistonExtensionBlock.createBlockEntityPiston(blockPos3, blockState3, list2.get(k), dir, retract, false));
                blockStates[j++] = blockState2;
            }

            BlockState blockState5 = Blocks.AIR.getDefaultState();

            for(BlockPos blockPos4 : movedBlocks.keySet()) {
                world.setBlockState(blockPos4, blockState5, Block.NOTIFY_LISTENERS | Block.FORCE_STATE | Block.MOVED);
            }

            for(Map.Entry<BlockPos, BlockState> entry : movedBlocks.entrySet()) {
                BlockPos blockPos5 = entry.getKey();
                BlockState blockState6 = entry.getValue();
                blockState6.prepare(world, blockPos5, 2);
                blockState5.updateNeighbors(world, blockPos5, Block.NOTIFY_LISTENERS);
                blockState5.prepare(world, blockPos5, 2);
            }

            j = 0;

            for(int l = pistonHandler.getBrokenBlocks().size() - 1; l >= 0; --l) {
                BlockState blockState2 = blockStates[j++];
                BlockPos blockPos5 = pistonHandler.getBrokenBlocks().get(l);
                blockState2.prepare(world, blockPos5, 2);
                world.updateNeighborsAlways(blockPos5, blockState2.getBlock());
            }

            for(int l = pistonHandler.getMovedBlocks().size() - 1; l >= 0; --l) {
                world.updateNeighborsAlways(pistonHandler.getMovedBlocks().get(l), blockStates[j++].getBlock());
            }

            return true;
        }
    }
}
