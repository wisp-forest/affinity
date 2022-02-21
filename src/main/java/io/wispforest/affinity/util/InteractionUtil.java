package io.wispforest.affinity.util;

import io.wispforest.owo.ops.ItemOps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class InteractionUtil {

    public static ActionResult handleSingleItemContainer(World world, BlockPos pos, PlayerEntity player, Hand hand,
                                                         Supplier<ItemStack> itemProvider, Consumer<ItemStack> itemManipulator, Runnable changeHandler) {
        return handleSingleItemContainer(world, pos, player, hand, stack -> true, InvalidBehaviour.DROP, itemProvider, itemManipulator, changeHandler);
    }

    public static ActionResult handleSingleItemContainer(World world, BlockPos pos, PlayerEntity player, Hand hand, Predicate<ItemStack> validator,
                                                         InvalidBehaviour invalidBehaviour, Supplier<ItemStack> itemProvider,
                                                         Consumer<ItemStack> itemManipulator, Runnable changeHandler) {
        var playerStack = player.getStackInHand(hand);
        var item = itemProvider.get();

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
                if (ItemOps.canStack(playerStack, item)) {
                    playerStack.increment(1);
                } else {
                    switch (invalidBehaviour) {
                        case DROP:
                            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), item);
                            break;
                        case DO_NOTHING:
                            return ActionResult.PASS;
                    }
                }

                itemManipulator.accept(ItemStack.EMPTY);
                changeHandler.run();
            }
        }

        return ActionResult.SUCCESS;
    }

    public enum InvalidBehaviour {
        DO_NOTHING, DROP
    }

}
