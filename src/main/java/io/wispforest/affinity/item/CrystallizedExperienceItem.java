package io.wispforest.affinity.item;

import io.wispforest.affinity.misc.util.ExperienceUtil;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.ops.ItemOps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CrystallizedExperienceItem extends Item {

    public CrystallizedExperienceItem() {
        super(AffinityItems.settings().trackUsageStat());
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity serverPlayer) {
            ExperienceUtil.updateExperience(
                    serverPlayer,
                    ExperienceUtil.totalPoints(serverPlayer) + ExperienceUtil.POINTS_30_LEVELS
            );
        }

        ItemOps.decrementPlayerHandItem(user, hand);
        return TypedActionResult.success(user.getStackInHand(hand));
    }

}
