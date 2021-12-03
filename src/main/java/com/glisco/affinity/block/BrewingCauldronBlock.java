package com.glisco.affinity.block;

import com.glisco.affinity.registries.AffinityBlocks;
import com.glisco.affinity.util.potion.PotionMixture;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BrewingCauldronBlock extends BlockWithEntity {

    private static final VoxelShape OUTLINE_SHAPE = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), VoxelShapes.union(
            createCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D),
            createCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D),
            createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D),
            createCuboidShape(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D)), BooleanBiFunction.ONLY_FIRST);

    public BrewingCauldronBlock() {
        super(FabricBlockSettings.copyOf(Blocks.CAULDRON));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BrewingCauldronBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, AffinityBlocks.Entities.BREWING_CAULDRON, BrewingCauldronBlockEntity::tick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        if (!(world.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity cauldron)) return ActionResult.PASS;
        final var playerStack = player.getStackInHand(hand);

        if (playerStack.getItem() instanceof PotionItem) {
            if (!cauldron.canPotionBeAdded()) return ActionResult.PASS;

            if (!world.isClient()) {
                cauldron.addOneBottle(PotionMixture.fromStack(playerStack));
                player.setStackInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
            }

            return ActionResult.SUCCESS;
        } else if (playerStack.isOf(Items.GLASS_BOTTLE)) {

            if (!cauldron.canPotionBeExtracted()) return ActionResult.PASS;

            if (!world.isClient()) {
                final var potionStack = cauldron.extractOneBottle();
                if (potionStack.isEmpty()) return ActionResult.PASS;

                player.setStackInHand(hand, potionStack);
            }

            return ActionResult.SUCCESS;
        } else if (playerStack.isEmpty()) {

            if (!cauldron.itemAvailable()) return ActionResult.PASS;

            if (!world.isClient()) {
                player.setStackInHand(hand, cauldron.getAndRemoveLast());
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
