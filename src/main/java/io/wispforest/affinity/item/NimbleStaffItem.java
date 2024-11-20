package io.wispforest.affinity.item;

import com.google.common.collect.ImmutableMap;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.client.render.InWorldTooltipProvider;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
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

    public static final ComponentType<Direction> DIRECTION = Affinity.component("nimble_staff_direction", CodecUtils.toEndec(Direction.CODEC));
    public static final ComponentType<BlockPos> ECHO_SHARD_TARGET = Affinity.component("nimble_staff_echo_shard_target", MinecraftEndecs.BLOCK_POS);

    private static final InquirableOutlineProvider.Outline UP_AOE = new InquirableOutlineProvider.Outline(-4, 0, -4, 4, 4, 4);
    private static final InquirableOutlineProvider.Outline DOWN_AOE = new InquirableOutlineProvider.Outline(-4, -4, -4, 4, 0, 4);

    private static final Map<Direction, Text> ARROW_BY_DIRECTION = new ImmutableMap.Builder<Direction, Text>()
            .put(Direction.NORTH, TextOps.withColor("↑", 0xb0ffce))
            .put(Direction.SOUTH, TextOps.withColor("↓", 0xb0ffce))
            .put(Direction.EAST, TextOps.withColor("→", 0xb0ffce))
            .put(Direction.WEST, TextOps.withColor("←", 0xb0ffce))
            .build();

    public NimbleStaffItem() {
        super(AffinityItems.settings().maxCount(1).trackUsageStat());
    }

    @Override
    public boolean canBePlacedOnPedestal() {
        return true;
    }

    @Override
    public void pedestalTickServer(ServerWorld world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        final var direction = getDirection(pedestal.getItem());
        var pushDelta = Vec3d.of(direction.getVector());

        var echoShardTarget = tryFindBoundEchoShard(pos, pedestal);
        if (echoShardTarget != null) {
            pushDelta = Vec3d.ofCenter(echoShardTarget).subtract(Vec3d.ofCenter(pos)).normalize();

            if (!echoShardTarget.equals(pedestal.getItem().get(ECHO_SHARD_TARGET))) {
                pedestal.getItem().set(ECHO_SHARD_TARGET, echoShardTarget);
                pedestal.markDirty();
            }
        } else if (pedestal.getItem().contains(ECHO_SHARD_TARGET)) {
            pedestal.getItem().remove(ECHO_SHARD_TARGET);
            pedestal.markDirty();
        }

        moveEntities(world, pos.add(0, pedestal.up() * 2, 0), pushDelta, () -> {
            if (!pedestal.hasFlux(5)) return false;

            pedestal.consumeFlux(5);
            return true;
        });
    }

    @Override
    public void pedestalTickClient(World world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        final var echoShardTarget = pedestal.getItem().get(ECHO_SHARD_TARGET);
        var pushDelta = echoShardTarget != null
                ? Vec3d.ofCenter(echoShardTarget).subtract(Vec3d.ofCenter(pos)).normalize()
                : Vec3d.of(getDirection(pedestal.getItem()).getVector());

        moveEntities(world, pos.add(0, pedestal.up() * 2, 0), pushDelta, () -> pedestal.hasFlux(5));

        if (!pedestal.hasFlux(5)) return;

        ClientParticles.setVelocity(pushDelta.multiply(.35));
        ClientParticles.setParticleCount(2);
        ClientParticles.spawnPrecise(ParticleTypes.CLOUD, world, Vec3d.ofCenter(pos, 1.5 * pedestal.up()), 9, 5, 9);
    }

    protected static void moveEntities(World world, BlockPos pos, Vec3d direction, BooleanSupplier shouldContinue) {
        var stuckPosition = Vec3d.ofCenter(pos).add(direction);
        var pushDelta = direction.multiply(.2);
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

    private static @Nullable BlockPos tryFindBoundEchoShard(BlockPos pos, StaffPedestalBlockEntity pedestal) {
        var storageBelow = ItemStorage.SIDED.find(pedestal.getWorld(), pos.add(0, pedestal.down(), 0), pedestal.facing().getOpposite());
        if (storageBelow == null) return null;

        BlockPos targetPos = null;
        for (var view : storageBelow) {
            if (!view.getResource().isOf(Items.ECHO_SHARD)) continue;

            // TODO: fabric transfer api is dumb. this shouldn't allocate every tick
            var components = new ComponentMapImpl(view.getResource().getItem().getComponents());
            components.applyChanges(view.getResource().getComponents());

            targetPos = EchoShardExtension.tryGetLocationInWorld(pedestal.getWorld(), () -> components);
            if (targetPos != null) break;
        }

        return targetPos;
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

            pedestal.getItem().set(DIRECTION, storedDirection);
            pedestal.markDirty();
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public InquirableOutlineProvider.Outline getAreaOfEffect(World world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        return pedestal.facing() == Direction.UP ? UP_AOE : DOWN_AOE;
    }

    @Override
    public void appendTooltipEntries(World world, BlockPos pos, StaffPedestalBlockEntity pedestal, List<InWorldTooltipProvider.Entry> entries) {
        if (pedestal.getItem().get(ECHO_SHARD_TARGET) != null) {
            var targetPos = pedestal.getItem().get(ECHO_SHARD_TARGET);
            entries.add(InWorldTooltipProvider.Entry.icon(
                    Text.literal(targetPos.getX() + " " + targetPos.getY() + " " + targetPos.getZ()),
                    16, 0
            ));
        } else {
            var direction = getDirection(pedestal.getItem());
            entries.add(InWorldTooltipProvider.Entry.text(
                    ARROW_BY_DIRECTION.get(direction),
                    Text.translatable(this.getTranslationKey() + ".direction." + direction.asString())
            ));
        }
    }

    private static Direction getDirection(ItemStack stack) {
        return stack.getOrDefault(DIRECTION, Direction.NORTH);
    }

    public static @Nullable BlockHitResult findFlingTarget(PlayerEntity player) {
        if (!player.isHolding(AffinityItems.NIMBLE_STAFF)) return null;

        var target = player.raycast(50, 1f, false);
        return target instanceof BlockHitResult blockHit && !player.getWorld().isAir(blockHit.getBlockPos())
                ? blockHit
                : null;
    }

    @Override
    protected TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks, @Nullable BlockPos clickedBlock) {
        var target = findFlingTarget(player);
        if (target == null) {
            return TypedActionResult.fail(stack);
        }

        player.getItemCooldownManager().set(this, 15);
        if (world.isClient) return TypedActionResult.success(stack);

        var targetCenter = Vec3d.ofCenter(target.getBlockPos());
        final var velocity = targetCenter.subtract(player.getPos()).multiply(
                player.isOnGround()
                        ? 0.15
                        : player.isFallFlying() ? 0.025 : 0.1
        );

        player.addVelocity(velocity.x, velocity.y, velocity.z);
        player.velocityModified = true;

        AffinityParticleSystems.NIMBLE_STAFF_FLING.spawn(world, player.getEyePos(), targetCenter);
        return TypedActionResult.success(stack);
    }

    @Override
    protected float getAethumConsumption(ItemStack stack) {
        return 2f;
    }
}
