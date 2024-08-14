package io.wispforest.affinity.block.impl;

import com.mojang.serialization.MapCodec;
import io.wispforest.affinity.block.template.BlockItemProvider;
import io.wispforest.affinity.blockentity.impl.MangroveBasketBlockEntity;
import io.wispforest.affinity.item.MangroveBasketItem;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
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
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MangroveBasketBlock extends BlockWithEntity implements BlockItemProvider {

    public MangroveBasketBlock() {
        super(FabricBlockSettings.copyOf(Blocks.MANGROVE_ROOTS));
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world.getBlockEntity(pos) instanceof MangroveBasketBlockEntity blockEntity) {
            if (!world.isClient && player.isCreative()) {
                ItemEntity itemEntity = new ItemEntity(world, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, blockEntity.toItem());
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            }
        }

        return super.onBreak(world, pos, state, player);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var stack = player.getStackInHand(hand);

        if (!stack.isOf(Items.FLINT_AND_STEEL)) return ActionResult.PASS;
        if (!(world.getBlockEntity(pos) instanceof MangroveBasketBlockEntity basket)) return ActionResult.PASS;

        var newState = basket.containedState();
        if (newState == null) return ActionResult.FAIL;

        MixinHooks.queuedBlockEntity = basket.containedBlockEntity();
        world.setBlockState(pos, newState, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);

        world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
        AffinityParticleSystems.LIGHT_BLOCK.spawn(world, Vec3d.ofCenter(pos));
        stack.damage(1, player, p -> p.sendToolBreakStatus(hand));

        return ActionResult.success(world.isClient);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        var list = super.getDroppedStacks(state, builder);

        if (builder.getOptional(LootContextParameters.BLOCK_ENTITY) instanceof MangroveBasketBlockEntity blockEntity) {
            list.add(blockEntity.toItem());
        }

        return list;
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return world.getBlockEntity(pos) instanceof MangroveBasketBlockEntity basket
                ? basket.toItem()
                : super.getPickStack(world, pos, state);
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

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }
}
