package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.AethumNetworkMemberBlock;
import io.wispforest.affinity.blockentity.impl.BrewingCauldronBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.misc.util.ListUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.ops.WorldOps;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BrewingCauldronBlock extends AethumNetworkMemberBlock {

    private static final VoxelShape OUTLINE_SHAPE = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), VoxelShapes.union(
            createCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D),
            createCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D),
            createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D),
            createCuboidShape(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D)), BooleanBiFunction.ONLY_FIRST);

    public BrewingCauldronBlock() {
        super(FabricBlockSettings.copyOf(Blocks.CAULDRON), CONSUMER_TOOLTIP);
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
        return validateTicker(type, AffinityBlocks.Entities.BREWING_CAULDRON, TickedBlockEntity.ticker());
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity cauldron)) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        final var playerStack = player.getStackInHand(hand);

        if (!playerStack.isEmpty() && ItemOps.canStack(playerStack, ListUtil.peekLast(cauldron.getItems()))) {

            if (!world.isClient()) {
                playerStack.increment(1);

                ListUtil.getAndRemoveLast(cauldron.getItems());
                cauldron.markDirty(false);
            }

            return ItemActionResult.SUCCESS;
        } else if (playerStack.getItem() instanceof PotionItem) {
            if (!cauldron.canPotionBeAdded()) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            if (!world.isClient()) {
                cauldron.addOneBottle(PotionMixture.fromStack(playerStack));
                player.setStackInHand(hand, new ItemStack(Items.GLASS_BOTTLE));

                WorldOps.playSound(world, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.PLAYERS);
            }

            return ItemActionResult.SUCCESS;
        } else if (playerStack.isOf(Items.GLASS_BOTTLE)) {

            if (!cauldron.canPotionBeExtracted()) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            if (!world.isClient()) {
                final var potionStack = cauldron.extractOneBottle();
                if (potionStack.isEmpty()) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

                ItemOps.decrementPlayerHandItem(player, hand);
                player.getInventory().offerOrDrop(potionStack);

                WorldOps.playSound(world, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.PLAYERS);
            }

            return ItemActionResult.SUCCESS;
        } else if (!playerStack.isEmpty()) {

            if (!cauldron.itemAvailable()) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            if (!world.isClient()) {
                final var item = new ItemEntity(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
                    ListUtil.getAndRemoveLast(cauldron.getItems()));
                item.getComponent(AffinityComponents.ENTITY_FLAGS).setFlag(EntityFlagComponent.SPAWNED_BY_BREWING_CAULDRON);

                world.spawnEntity(item);
                cauldron.markDirty(false);
            }

            return ItemActionResult.SUCCESS;
        } else {

            if (!cauldron.itemAvailable()) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            if (!world.isClient()) {
                player.setStackInHand(hand, ListUtil.getAndRemoveLast(cauldron.getItems()));
                cauldron.markDirty(false);
            }

            return ItemActionResult.SUCCESS;
        }
    }
}
