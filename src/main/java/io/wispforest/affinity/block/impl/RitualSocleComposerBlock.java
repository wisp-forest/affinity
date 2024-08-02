package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.DamageTypeKey;
import io.wispforest.affinity.misc.screenhandler.RitualSocleComposerScreenHandler;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RitualSocleComposerBlock extends Block {

    public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;

    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 10, 16);
    private static final DamageTypeKey DAMAGE_TYPE = new DamageTypeKey(Affinity.id("sawblade"));

    public RitualSocleComposerBlock() {
        super(FabricBlockSettings.copyOf(Blocks.STONECUTTER));
        this.setDefaultState(this.getDefaultState().with(AXIS, Direction.Axis.X));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(AXIS, ctx.getHorizontalPlayerFacing().rotateYClockwise().getAxis());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        player.openHandledScreen(new ScreenHandlerFactory(world, pos));
        return ActionResult.SUCCESS;
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.bypassesSteppingEffects() && entity instanceof LivingEntity) {
            entity.damage(DAMAGE_TYPE.source(world), 1f);
        }

        super.onSteppedOn(world, pos, state, entity);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    private record ScreenHandlerFactory(World world, BlockPos pos) implements NamedScreenHandlerFactory {

        @Override
        public Text getDisplayName() {
            return AffinityBlocks.RITUAL_SOCLE_COMPOSER.getName();
        }

        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
            return new RitualSocleComposerScreenHandler(syncId, inv, ScreenHandlerContext.create(world, pos));
        }
    }
}
