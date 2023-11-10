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
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class UnfloweringAzaleaLeavesBlock extends LeavesBlock {

    public UnfloweringAzaleaLeavesBlock() {
        super(FabricBlockSettings.copyOf(Blocks.FLOWERING_AZALEA_LEAVES));
        this.setDefaultState(this.getDefaultState().with(PERSISTENT, true));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.isSneaking()) return ActionResult.PASS;

        if (!world.isClient) {
            unflower(world, pos);
            WorldOps.breakBlockWithItem(world, pos, ItemStack.EMPTY);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        final var dust = new DustParticleEffect(MathUtil.rgbToVec3f(0x5548ce), 1);

        ClientParticles.setParticleCount(15);
        ClientParticles.spawnCenteredOnBlock(dust, world, pos, 1.25);
    }

    public static void unflower(World world, BlockPos leavesPos) {
        for (var testPos : BlockPos.iterate(leavesPos.add(16, 3, 16), leavesPos.add(-16, -3, -16))) {
            final var testState = world.getBlockState(testPos);
            if (!(testState.getBlock() instanceof PlantBlock)) continue;

            Block.dropStacks(testState, world, testPos);
            world.removeBlock(testPos, false);
        }
    }
}
