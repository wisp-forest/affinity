package io.wispforest.affinity.block.template;

import io.wispforest.affinity.blockentity.impl.AethumFluxNodeBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAethumFluxNodeBlock extends AethumNetworkMemberBlock implements AttackInteractionReceiver {

    protected AbstractAethumFluxNodeBlock() {
        super(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS).nonOpaque().luminance(10).solid(), NODE_TOOLTIP);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AethumFluxNodeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, AffinityBlocks.Entities.AETHUM_FLUX_NODE, TickedBlockEntity.ticker());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return InteractableBlockEntity.tryHandle(world, pos, player, hand, hit);
    }

    @Override
    public @NotNull ActionResult onAttack(World world, BlockState state, BlockPos pos, PlayerEntity player) {
        if (!(world.getBlockEntity(pos) instanceof AethumFluxNodeBlockEntity node)) return ActionResult.PASS;
        return node.onAttack(player);
    }

    public abstract boolean isUpgradeable();

    public abstract float shardHeight();

    public Vec3d linkAttachmentPoint() {
        return Vec3d.ZERO;
    }
}
