package io.wispforest.affinity.blockentity.template;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface InteractableBlockEntity {

    ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit);

    static ActionResult tryHandle(World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof InteractableBlockEntity entity)) return ActionResult.PASS;
        return entity.onUse(player, hand, hit);
    }

    static ItemActionResult tryHandleWithItem(World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        // i love me some instant legacy code
        return switch (InteractableBlockEntity.tryHandle(world, pos, player, hand, hit)) {
            case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemActionResult.SUCCESS;
            case CONSUME -> ItemActionResult.CONSUME;
            case CONSUME_PARTIAL -> ItemActionResult.CONSUME_PARTIAL;
            case FAIL -> ItemActionResult.FAIL;
            case PASS -> ItemActionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        };
    }

}
