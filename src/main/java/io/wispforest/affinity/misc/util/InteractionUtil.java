package io.wispforest.affinity.misc.util;

import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.owo.ops.ItemOps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class InteractionUtil {

    public static EntityHitResult raycastEntities(Entity entity, double reach, double margin, Predicate<Entity> predicate) {
        var maxReach = entity.getRotationVec(0).multiply(reach);

        MixinHooks.extraTargetingMargin = margin;
        var entityTarget = ProjectileUtil.raycast(
                entity,
                entity.getEyePos(),
                entity.getEyePos().add(maxReach),
                entity.getBoundingBox().stretch(maxReach),
                candidate -> {
                    if (candidate.isSpectator()) return false;
                    return predicate.test(candidate);
                },
                reach * reach
        );
        MixinHooks.extraTargetingMargin = 0;

        return entityTarget;
    }

    public static ActionResult handleSingleItemContainer(
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            Supplier<ItemStack> itemProvider,
            Consumer<ItemStack> itemManipulator,
            Runnable changeHandler
    ) {
        return handleSingleItemContainer(
                world,
                pos,
                player,
                hand,
                stack -> true,
                InvalidBehaviour.DROP,
                itemProvider,
                itemManipulator,
                changeHandler
        );
    }

    public static ActionResult handleSingleItemContainer(
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            Predicate<ItemStack> validator,
            InvalidBehaviour invalidBehaviour,
            Supplier<ItemStack> itemProvider,
            Consumer<ItemStack> itemManipulator,
            Runnable changeHandler
    ) {
        var playerStack = player.getStackInHand(hand);
        var item = itemProvider.get().copy();

        if (playerStack.isEmpty()) {
            if (item.isEmpty()) return ActionResult.PASS;
            if (world.isClient()) return ActionResult.SUCCESS;

            player.setStackInHand(hand, item.copy());
            itemManipulator.accept(ItemStack.EMPTY);

            changeHandler.run();
        } else {
            if (world.isClient()) return validator.test(playerStack) ? ActionResult.SUCCESS : ActionResult.PASS;

            if (item.isEmpty()) {
                if (!validator.test(playerStack)) return ActionResult.PASS;

                itemManipulator.accept(ItemOps.singleCopy(playerStack));
                ItemOps.decrementPlayerHandItem(player, hand);

                changeHandler.run();
            } else {
                if (ItemStack.canCombine(playerStack, item)) {
                    int incrementCount = Math.min(playerStack.getMaxCount() - playerStack.getCount(), item.getCount());

                    playerStack.increment(incrementCount);
                    item.decrement(incrementCount);
                }

                if (item.isEmpty()) {
                    item = ItemStack.EMPTY;
                } else {
                    switch (invalidBehaviour) {
                        case DROP -> {
                            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), item);
                            item = ItemStack.EMPTY;
                        }
                        case DO_NOTHING -> {
                            return ActionResult.PASS;
                        }
                    }
                }

                itemManipulator.accept(item);
                changeHandler.run();
            }
        }

        return ActionResult.SUCCESS;
    }

    public enum InvalidBehaviour {
        DO_NOTHING, DROP
    }

}
