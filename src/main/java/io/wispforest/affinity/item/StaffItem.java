package io.wispforest.affinity.item;

import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.client.render.InWorldTooltipProvider;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class StaffItem extends Item implements SpecialTransformItem {

    protected StaffItem(Settings settings) {
        super(settings);
    }

    // -------
    // In Hand
    // -------

    protected abstract TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks, @Nullable BlockPos clickedBlock);

    protected abstract float getAethumConsumption(ItemStack stack);

    protected boolean isContinuous(ItemStack stack) {
        return false;
    }

    protected @Nullable Text getModeName(ItemStack stack) {
        return null;
    }

    // -----------
    // On Pedestal
    // -----------

    public boolean canBePlacedOnPedestal() {
        return false;
    }

    public void pedestalTickClient(World world, BlockPos pos, StaffPedestalBlockEntity pedestal) {}

    public void pedestalTickServer(ServerWorld world, BlockPos pos, StaffPedestalBlockEntity pedestal) {}

    public void appendTooltipEntries(World world, BlockPos pos, StaffPedestalBlockEntity pedestal, List<InWorldTooltipProvider.Entry> entries) {}

    public ActionResult onPedestalScrolled(World world, BlockPos pos, StaffPedestalBlockEntity pedestal, boolean direction) {
        return ActionResult.PASS;
    }

    public @Nullable InquirableOutlineProvider.Outline getAreaOfEffect() {
        return null;
    }

    // --------------
    // Implementation
    // --------------

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return this.handleItemUse(world, user, hand, null);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return this.handleItemUse(context.getWorld(), context.getPlayer(), context.getHand(), context.getBlockPos()).getResult();
    }

    private TypedActionResult<ItemStack> handleItemUse(World world, PlayerEntity user, Hand hand, @Nullable BlockPos clickedBlock) {
        final var stack = user.getStackInHand(hand);

        final var aethum = user.getComponent(AffinityComponents.PLAYER_AETHUM);
        final var consumption = this.getAethumConsumption(stack);

        if (this.isContinuous(stack)) {
            if (aethum.getAethum() < consumption * 20 && !user.isCreative()) return TypedActionResult.pass(stack);

            if (this.executeSpell(world, user, stack, this.getMaxUseTime(stack), clickedBlock).getResult().isAccepted()) {
                user.setCurrentHand(hand);
                return TypedActionResult.consume(stack);
            } else {
                return TypedActionResult.pass(stack);
            }
        } else {
            if (aethum.getAethum() < consumption && !user.isCreative()) return TypedActionResult.pass(stack);

            var result = this.executeSpell(world, user, stack, -1, clickedBlock);
            if (result.getResult().isAccepted()) aethum.addAethum(-consumption);

            return result;
        }
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        final var aethum = player.getComponent(AffinityComponents.PLAYER_AETHUM);
        if (!aethum.tryConsumeAethum(this.getAethumConsumption(stack))) {
            user.stopUsingItem();
            return;
        }

        if (!this.executeSpell(world, player, stack, remainingUseTicks, null).getResult().isAccepted()) {
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
    public int getMaxUseTime(ItemStack stack) {
        return this.isContinuous(stack)
                ? 72000
                : 0;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void applyUseActionTransform(ItemStack stack, AbstractClientPlayerEntity player, MatrixStack matrices, float tickDelta, float swingProgress) {
        matrices.translate(-.5, -.5, -.5);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-45 + (float) Math.sin((player.clientWorld.getTime() + tickDelta) / 20)));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) Math.sin((player.clientWorld.getTime() + tickDelta) / 30)));
        matrices.translate(.5, .75, .5);
    }

    @Override
    public void applyUseActionLeftArmPose(ItemStack stack, AbstractClientPlayerEntity player, PlayerEntityModel<AbstractClientPlayerEntity> model) {
        model.leftArm.yaw = .1f + model.head.yaw;
        model.leftArm.pitch = -(float) Math.PI / 3.5f + model.head.pitch * .5f;
    }

    @Override
    public void applyUseActionRightArmPose(ItemStack stack, AbstractClientPlayerEntity player, PlayerEntityModel<AbstractClientPlayerEntity> model) {
        model.rightArm.yaw = -.1f + model.head.yaw;
        model.rightArm.pitch = -(float) Math.PI / 3.5f + model.head.pitch * .5f;
    }
}
