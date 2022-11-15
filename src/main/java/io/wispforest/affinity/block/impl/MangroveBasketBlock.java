package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.BlockItemProvider;
import io.wispforest.affinity.blockentity.impl.MangroveBasketBlockEntity;
import io.wispforest.affinity.item.MangroveBasketItem;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MangroveBasketBlock extends BlockWithEntity implements BlockItemProvider {

    public MangroveBasketBlock() {
        super(FabricBlockSettings.copyOf(Blocks.MANGROVE_ROOTS));
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world.getBlockEntity(pos) instanceof MangroveBasketBlockEntity blockEntity) {
            if (!world.isClient && player.isCreative()) {
                ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, blockEntity.toItem());
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            }
        }

        super.onBreak(world, pos, state, player);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var stack = player.getStackInHand(hand);

        if (!stack.isOf(Items.FLINT_AND_STEEL)) return ActionResult.PASS;

        if (!(world.getBlockEntity(pos) instanceof MangroveBasketBlockEntity blockEntity)) return ActionResult.PASS;

        var newState = blockEntity.getContainedState();

        if (newState == null) return ActionResult.FAIL;

        world.setBlockState(pos, newState, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
        world.addBlockEntity(blockEntity.getContainedBlockEntity());

        world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
        AffinityParticleSystems.LIGHT_BLOCK.spawn(world, Vec3d.ofCenter(pos));
        stack.damage(1, player, p -> p.sendToolBreakStatus(hand));

        return ActionResult.success(world.isClient);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world.isClient) return;
        if (BlockItem.getBlockEntityNbt(itemStack) == null) return;

        if (world.getBlockEntity(pos) instanceof MangroveBasketBlockEntity blockEntity) {
            blockEntity.onPlaced(placer);
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        var list = super.getDroppedStacks(state, builder);

        if (builder.getNullable(LootContextParameters.BLOCK_ENTITY) instanceof MangroveBasketBlockEntity blockEntity) {
            list.add(blockEntity.toItem());
        }

        return list;
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof MangroveBasketBlockEntity blockEntity)
            return blockEntity.toItem();

        return super.getPickStack(world, pos, state);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MangroveBasketBlockEntity(pos, state);
    }

    @Override
    public Item createBlockItem(Block block, OwoItemSettings settings) {
        return new MangroveBasketItem(block, settings);
    }
}
