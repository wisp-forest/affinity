package io.wispforest.affinity.block.template;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public interface AttackInteractionReceiver {
    @NotNull ActionResult onAttack(World world, BlockState state, BlockPos pos, PlayerEntity player);

    record InteractionPacket(BlockPos pos) {}
}
