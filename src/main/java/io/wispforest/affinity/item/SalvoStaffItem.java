package io.wispforest.affinity.item;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.object.AffinityEntities;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinitySoundEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SalvoStaffItem extends StaffItem {

    private static final int MISSILE_COST = 1;

    public SalvoStaffItem() {
        super(AffinityItems.settings().maxCount(1));
    }

    @Override
    protected TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks, @Nullable BlockPos clickedBlock) {
        var aethum = player.getComponent(AffinityComponents.PLAYER_AETHUM);
        if (!aethum.hasAethum(MISSILE_COST)) return TypedActionResult.pass(stack);

        EntityHitResult target = null;
        for (int i = 0; i < 5; i++) {
            target = InteractionUtil.raycastEntities(player, 1f, 25, .5 * i, entity -> entity.getType() != AffinityEntities.AETHUM_MISSILE && entity.isAlive() && entity instanceof LivingEntity);
            if (target != null) break;
        }

        if (target == null) return TypedActionResult.pass(stack);

        if (remainingTicks % 2 != 0 || remainingTicks % 20 < 12) return TypedActionResult.success(stack);
        if (!aethum.tryConsumeAethum(MISSILE_COST)) return TypedActionResult.pass(stack);

        if (!world.isClient) {
            var missile = AffinityEntities.AETHUM_MISSILE.create(world);
            missile.setPosition(player.getEyePos().add(player.getRotationVec(0).multiply(2)).subtract(0, .5, 0));
            missile.setTargetEntity(target.getEntity());
            missile.setOwner(player);

            world.spawnEntity(missile);
        } else {
            player.playSound(AffinitySoundEvents.ITEM_SALVO_STAFF_FIRE_MISSILE, 1f, 2f);
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
        if (!(user instanceof PlayerEntity player)) return;

        int cooldown = remainingUseTicks % 20;
        if (cooldown < 1) return;

        player.getItemCooldownManager().set(this, cooldown * 2);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable(
                this.getTranslationKey() + ".tooltip.consumption_per_missile",
                MISSILE_COST
        ));
    }

    @Override
    protected float getAethumConsumption(ItemStack stack) {
        return 0f;
    }

    @Override
    protected boolean isContinuous(ItemStack stack) {
        return true;
    }
}
