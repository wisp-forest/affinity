package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.misc.screenhandler.OuijaBoardScreenHandler;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class OuijaBoardBlock extends Block {

    public OuijaBoardBlock() {
        super(FabricBlockSettings.copyOf(Blocks.ENCHANTING_TABLE));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            player.openHandledScreen(new NamedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return state.getBlock().getName();
                }

                @Override
                public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new OuijaBoardScreenHandler(syncId, inv, ScreenHandlerContext.create(world, pos));
                }
            });
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return Blocks.ENCHANTING_TABLE.getOutlineShape(state, world, pos, context);
    }
}
