package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.ops.ItemOps;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class MilkCupItem extends Item {

    private static final int MAX_USE_TIME = 32;

    public MilkCupItem() {
        super(AffinityItems.settings(AffinityItemGroup.MAIN).maxCount(8));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {
            user.clearStatusEffects();
        }

        if (user instanceof PlayerEntity player) {
            if (player instanceof ServerPlayerEntity serverPlayer) Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);
            player.incrementStat(Stats.USED.getOrCreateStat(this));

            if (!player.isCreative() && ItemOps.emptyAwareDecrement(stack)) {
                player.getInventory().offerOrDrop(AffinityItems.CLAY_CUP.getDefaultStack());
            }
        }

        return stack.isEmpty()
                ? AffinityItems.CLAY_CUP.getDefaultStack()
                : stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return MAX_USE_TIME;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    static {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity.getType() != EntityType.COW || ((CowEntity) entity).isBaby()) return ActionResult.PASS;

            var playerStack = player.getStackInHand(hand);
            if (!playerStack.isOf(AffinityItems.CLAY_CUP)) return ActionResult.PASS;

            player.playSound(SoundEvents.ENTITY_COW_MILK, 1f, 1f);
            player.setStackInHand(hand, ItemUsage.exchangeStack(playerStack, player, AffinityItems.MILK_CUP.getDefaultStack()));
            return ActionResult.success(entity.getWorld().isClient);
        });
    }
}
