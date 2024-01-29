package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.template.AethumNetworkMemberBlock;
import io.wispforest.affinity.blockentity.impl.FieldCoherenceModulatorBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FieldCoherenceModulatorBlock extends AethumNetworkMemberBlock {

    private static final Identifier INTERACT_WITH_FIELD_COHERENCE_MODULATOR = Affinity.id("interact_with_field_coherence_modulator");
    private static final VoxelShape SHAPE = Block.createCuboidShape(4, 4, 4, 12, 12, 12);

    public FieldCoherenceModulatorBlock() {
        super(FabricBlockSettings.copyOf(Blocks.DEEPSLATE_TILES).nonOpaque(), CONSUMER_TOOLTIP);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FieldCoherenceModulatorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, AffinityBlocks.Entities.FIELD_COHERENCE_MODULATOR, TickedBlockEntity.ticker());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        player.incrementStat(INTERACT_WITH_FIELD_COHERENCE_MODULATOR);
        return InteractableBlockEntity.tryHandle(world, pos, player, hand, hit);
    }

    static {
        Registry.register(Registries.CUSTOM_STAT, INTERACT_WITH_FIELD_COHERENCE_MODULATOR, INTERACT_WITH_FIELD_COHERENCE_MODULATOR);
        Stats.CUSTOM.getOrCreateStat(INTERACT_WITH_FIELD_COHERENCE_MODULATOR);
    }
}
