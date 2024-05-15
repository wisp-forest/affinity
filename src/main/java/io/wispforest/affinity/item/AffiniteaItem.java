package io.wispforest.affinity.item;

import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityStatusEffects;
import io.wispforest.owo.ops.ItemOps;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class AffiniteaItem extends Item {

    private static final int MAX_USE_TIME = 32;
    private static final Supplier<StatusEffectInstance> EFFECT = () -> new StatusEffectInstance(AffinityStatusEffects.AFFINE, 5 * 60 * 20);

    public AffiniteaItem() {
        super(AffinityItems.settings().maxCount(8));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {
            user.addStatusEffect(EFFECT.get());
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

}
