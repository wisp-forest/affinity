package io.wispforest.affinity.item;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.object.AffinityEntities;
import io.wispforest.affinity.object.AffinityItems;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SalvoStaffItem extends StaffItem {

    private static final int MISSILE_COST = 1;

    public SalvoStaffItem() {
        super(AffinityItems.settings(AffinityItemGroup.MAIN).maxCount(1));
    }

    @Override
    protected TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks, @Nullable BlockPos clickedBlock) {
        var aethum = AffinityComponents.PLAYER_AETHUM.get(player);
        if (aethum.getAethum() < MISSILE_COST) return TypedActionResult.pass(stack);

        var target = InteractionUtil.raycastEntities(player, 15, 2, entity -> entity.getType() != AffinityEntities.AETHUM_MISSILE && entity.isAlive() && entity instanceof LivingEntity);
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
            player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_STEP, 1f, 2f);
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
        if (!(user instanceof PlayerEntity player)) return;

        int cooldown = remainingUseTicks % 20;
        if (cooldown < 1) return;

        player.getItemCooldownManager().set(this, cooldown);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
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
