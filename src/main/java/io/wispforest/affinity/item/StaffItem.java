package io.wispforest.affinity.item;

import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.util.MathUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class StaffItem extends Item {

    protected StaffItem(Settings settings) {
        super(settings);
    }

    protected abstract TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks);

    protected abstract float getAethumConsumption(ItemStack stack);

    protected boolean isContinuous(ItemStack stack) {
        return false;
    }

    protected @Nullable Text getModeName(ItemStack stack) {
        return null;
    }

    public boolean canBePlacedOnPedestal() {
        return false;
    }

    public void pedestalTickServer(World world, BlockPos pos, StaffPedestalBlockEntity pedestal) {}
    public void pedestalTickClient(World world, BlockPos pos, StaffPedestalBlockEntity pedestal) {}

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        final var stack = user.getStackInHand(hand);

        final var aethum = AffinityComponents.PLAYER_AETHUM.get(user);
        final var consumption = this.getAethumConsumption(stack);

        if (this.isContinuous(stack)) {
            if (aethum.getAethum() < consumption * 20) return TypedActionResult.pass(stack);
            user.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        } else {
            if (aethum.getAethum() < consumption) return TypedActionResult.pass(stack);

            aethum.addAethum(-consumption);
            return this.executeSpell(world, user, stack, -1);
        }
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        final var aethum = AffinityComponents.PLAYER_AETHUM.get(player);
        final var consumption = this.getAethumConsumption(stack);
        if (aethum.getAethum() < consumption) {
            user.stopUsingItem();
            return;
        }

        aethum.addAethum(-consumption);
        if (!this.executeSpell(world, player, stack, remainingUseTicks).getResult().isAccepted()) {
            user.stopUsingItem();
        }
    }

    @Override
    public Text getName(ItemStack stack) {
        final var modeName = this.getModeName(stack);
        if (modeName == null) return super.getName(stack);

        return Text.translatable(this.getTranslationKey()).append(Text.translatable(
                "item.affinity.staff.mode_template",
                modeName
        ));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (this.isContinuous(stack)) {
            tooltip.add(Text.translatable(
                    "item.affinity.staff.tooltip.consumption_per_second",
                    MathUtil.rounded(this.getAethumConsumption(stack) * 20, 2)
            ));
        } else {
            tooltip.add(Text.translatable(
                    "item.affinity.staff.tooltip.consumption_per_use",
                    MathUtil.rounded(this.getAethumConsumption(stack), 2)
            ));
        }

        if (this.getModeName(stack) != null) {
            tooltip.add(Text.empty());
            tooltip.add(Text.translatable("item.affinity.staff.tooltip.cycle_mode_hint"));
        }
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return this.isContinuous(stack)
                ? UseAction.BOW
                : UseAction.NONE;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return this.isContinuous(stack)
                ? 72000
                : 0;
    }

}
