package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.blockentity.impl.OuijaBoardBlockEntity;
import io.wispforest.affinity.misc.screenhandler.OuijaBoardScreenHandler;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.util.VectorRandomUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OuijaBoardBlock extends BlockWithEntity {

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

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextFloat() < .5f) return;

        for (int i = 0; i < 3; i++) {
            var origin = VectorRandomUtils.getRandomCenteredOnBlock(world, pos.up(1), 3);

            ClientParticles.setVelocity(Vec3d.ofCenter(pos, .9f).subtract(origin));
            ClientParticles.spawn(ParticleTypes.ENCHANT, world, origin, 0f);
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new OuijaBoardBlockEntity(pos, state);
    }
}
