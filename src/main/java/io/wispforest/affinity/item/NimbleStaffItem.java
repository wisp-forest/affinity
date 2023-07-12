package io.wispforest.affinity.item;

import com.google.common.collect.ImmutableMap;
import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.client.render.InWorldTooltipProvider;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class NimbleStaffItem extends StaffItem {

    public static final NbtKey<Direction> DIRECTION = new NbtKey<>("Direction", NbtKey.Type.STRING.then(Direction::byName, Direction::asString));

    private static final InquirableOutlineProvider.Outline AOE = InquirableOutlineProvider.Outline.symmetrical(4, 2, 4);
    private static final Map<Direction, Text> ARROW_BY_DIRECTION = new ImmutableMap.Builder<Direction, Text>()
            .put(Direction.NORTH, TextOps.withColor("↑", 0xFEF5AC))
            .put(Direction.SOUTH, TextOps.withColor("↓", 0xFEF5AC))
            .put(Direction.EAST, TextOps.withColor("→", 0xFEF5AC))
            .put(Direction.WEST, TextOps.withColor("←", 0xFEF5AC))
            .build();

    public NimbleStaffItem() {
        super(AffinityItems.settings(AffinityItemGroup.EQUIPMENT).maxCount(1));
    }

    @Override
    public boolean canBePlacedOnPedestal() {
        return true;
    }

    @Override
    public void pedestalTickServer(ServerWorld world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        moveEntities(world, pos, pedestal, () -> {
            if (!pedestal.hasFlux(25)) return false;

            pedestal.consumeFlux(25);
            return true;
        });
    }

    @Override
    public void pedestalTickClient(World world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        moveEntities(world, pos, pedestal, () -> pedestal.hasFlux(25));
    }

    protected static void moveEntities(World world, BlockPos pos, StaffPedestalBlockEntity pedestal, BooleanSupplier shouldContinue) {
        final var direction = getDirection(pedestal.getItem());

        var pushDelta = Vec3d.of(direction.getVector()).multiply(.2);
        var stuckPosition = Vec3d.ofCenter(pos.offset(direction.getOpposite()));
        var unstuckDelta = pushDelta.rotateY(45);

        for (var entity : world.getNonSpectatingEntities(Entity.class, new Box(pos).expand(4, 2, 4))) {
            if (entity.isSneaking()) continue;
            if (!shouldContinue.getAsBoolean()) return;

            if (entity.getPos().isInRange(stuckPosition, 1.5)) {
                entity.addVelocity(unstuckDelta);
            } else {
                entity.addVelocity(pushDelta);
            }
        }
    }

    @Override
    public ActionResult onPedestalScrolled(World world, BlockPos pos, StaffPedestalBlockEntity pedestal, boolean direction) {
        if (!world.isClient) {
            var storedDirection = getDirection(pedestal.getItem());
            if (storedDirection.getAxis() == Direction.Axis.Y) {
                storedDirection = Direction.NORTH;
            } else {
                storedDirection = direction
                        ? storedDirection.rotateYClockwise()
                        : storedDirection.rotateYCounterclockwise();
            }

            pedestal.getItem().put(DIRECTION, storedDirection);
            pedestal.markDirty();
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public @Nullable InquirableOutlineProvider.Outline getAreaOfEffect() {
        return AOE;
    }

    @Override
    public void appendTooltipEntries(World world, BlockPos pos, StaffPedestalBlockEntity pedestal, List<InWorldTooltipProvider.Entry> entries) {
        var direction = getDirection(pedestal.getItem());
        entries.add(InWorldTooltipProvider.Entry.text(
                ARROW_BY_DIRECTION.get(direction),
                Text.translatable(this.getTranslationKey() + ".direction." + direction.asString())
        ));
    }

    public static Direction getDirection(ItemStack stack) {
        return stack.getOr(DIRECTION, Direction.NORTH);
    }

    @Override
    protected TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks, @Nullable BlockPos clickedBlock) {
        var target = player.raycast(50, 0, false);
        if (!(target instanceof BlockHitResult blockHit)) return TypedActionResult.fail(stack);
        if (world.isAir(blockHit.getBlockPos())) return TypedActionResult.fail(stack);

        var targetCenter = Vec3d.ofCenter(blockHit.getBlockPos());

        final var velocity = targetCenter.subtract(player.getPos()).multiply(
                player.isOnGround()
                        ? 0.15
                        : player.isFallFlying() ? 0.025 : 0.1
        );
        player.addVelocity(velocity.x, velocity.y, velocity.z);

        if (world.isClient) return TypedActionResult.success(stack);
        AffinityParticleSystems.NIMBLE_STAFF_FLING.spawn(world, player.getEyePos(), targetCenter);

        player.getItemCooldownManager().set(this, 15);
        return TypedActionResult.success(stack);
    }

    @Override
    protected float getAethumConsumption(ItemStack stack) {
        return 2f;
    }
}
