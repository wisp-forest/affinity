package io.wispforest.affinity.item;

import io.wispforest.affinity.misc.util.ExperienceUtil;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.ops.ItemOps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class BuddingExperienceCrystalItem extends Item {

    public BuddingExperienceCrystalItem() {
        super(AffinityItems.settings());
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.experienceLevel < 30) return TypedActionResult.pass(user.getStackInHand(hand));

        user.setCurrentHand(hand);
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!(user instanceof PlayerEntity player) || player.experienceLevel < 30) return stack;

        if (player instanceof ServerPlayerEntity serverPlayer) {
            ExperienceUtil.updateExperience(
                    serverPlayer,
                    ExperienceUtil.totalPoints(player) - ExperienceUtil.POINTS_30_LEVELS
            );
        }

        player.getInventory().offerOrDrop(AffinityItems.CRYSTALLIZED_EXPERIENCE.getDefaultStack());
        return ItemOps.emptyAwareDecrement(stack) ? stack : ItemStack.EMPTY;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 30;
    }
}
