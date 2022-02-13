package io.wispforest.affinity.blockentity.template;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface InteractableBlockEntity {

    ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit);

    static ActionResult tryHandle(World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof InteractableBlockEntity entity)) return ActionResult.PASS;
        return entity.onUse(player, hand, hit);
    }

}
