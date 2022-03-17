package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class UnfloweringAzaleaLeavesBlock extends LeavesBlock {

    public UnfloweringAzaleaLeavesBlock() {
        super(FabricBlockSettings.copyOf(Blocks.FLOWERING_AZALEA_LEAVES));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.isSneaking()) return ActionResult.PASS;

        if (!world.isClient) {
            for (var testPos : BlockPos.iterate(pos.add(16, 3, 16), pos.add(-16, -3, -16))) {
                final var testState = world.getBlockState(testPos);
                if (!(testState.getBlock() instanceof PlantBlock)) continue;

                Block.dropStacks(testState, world, testPos);
                world.removeBlock(testPos, false);
            }
            WorldOps.breakBlockWithItem(world, pos, ItemStack.EMPTY);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        final var dust = new DustParticleEffect(MathUtil.splitRGBToVector(0x5548ce), 1);

        ClientParticles.setParticleCount(15);
        ClientParticles.spawnCenteredOnBlock(dust, world, pos, 1.25);
    }
}
