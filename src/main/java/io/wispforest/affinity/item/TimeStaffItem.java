package io.wispforest.affinity.item;

import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TimeStaffItem extends StaffItem implements DirectInteractionHandler {

    public static final NbtKey<Mode> MODE = new NbtKey<>("Mode", NbtKey.Type.STRING.then(Mode::byId, mode -> mode.id));

    public TimeStaffItem() {
        super(AffinityItems.settings(AffinityItemGroup.MAIN).maxCount(1));
    }

    @Override
    protected float getAethumConsumption(ItemStack stack) {
        return stack.get(MODE).aethumDrain;
    }

    @Override
    protected boolean isContinuous(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canBePlacedOnPedestal() {
        return true;
    }

    @Override
    public void pedestalTickServer(ServerWorld world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        var mode = pedestal.getItem().get(MODE);
        var costPerBlock = (long) (mode.aethumDrain * 100);

        for (var targetPos : BlockPos.iterate(pos.add(-2, -1, -2), pos.add(2, 1, 2))) {
            if (targetPos.equals(pos)) continue;

            if (!pedestal.hasFlux(costPerBlock)) {
                return;
            }

            if (accelerate(world, targetPos, mode.repeatTicks)) {
                pedestal.consumeFlux(costPerBlock);
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking()) {
            var stack = user.getStackInHand(hand);
            if (!world.isClient) stack.mutate(MODE, Mode::next);
            return TypedActionResult.success(stack, world.isClient);
        }

        return super.use(world, user, hand);
    }

    @Override
    protected TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks) {
        if (world.isClient) return TypedActionResult.consume(stack);

        var res = (BlockHitResult) player.raycast(5, 0, false);

        if (world.random.nextInt(4) == 0)
            AffinityParticleSystems.TIME_STAFF_ACCELERATE.spawn(world, player.getPos().add(0, 1.25, 0), res.getBlockPos());

        var mode = stack.get(MODE);
        accelerate(world, res.getBlockPos(), mode.repeatTicks);

        return TypedActionResult.consume(stack);
    }

    protected static boolean accelerate(World world, BlockPos pos, int ticks) {
        boolean ticked = false;

        for (int i = 0; i < ticks; i++) {
            BlockState state = world.getBlockState(pos);
            BlockEntity be = world.getBlockEntity(pos);

            if (be != null) {
                var ticker = state.getBlockEntityTicker(world, (BlockEntityType<BlockEntity>) be.getType());

                if (ticker != null) {
                    ticker.tick(world, pos, state, be);
                    ticked = true;
                }
            }

            if (state.hasRandomTicks()) {
                int randomTickPeriod = 4096 / world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);

                ticked = true;
                if (world.random.nextInt(randomTickPeriod) == 0) {
                    state.randomTick((ServerWorld) world, pos, world.random);
                }
            }

            if (!ticked) return false;
        }

        return ticked;
    }

    @Override
    protected @Nullable Text getModeName(ItemStack stack) {
        var mode = stack.get(MODE);
        return Text.translatable(this.getTranslationKey() + ".mode." + mode.id, mode.repeatTicks + 1);
    }

    @Override
    public boolean shouldHandleInteraction(World world, BlockPos pos, BlockState state) {
        return !state.isOf(AffinityBlocks.STAFF_PEDESTAL);
    }

    public enum Mode {
        NORMAL("normal", 1, 0.1f),
        FAST("fast", 3, 0.4f),
        LUDICROUS("ludicrous", 7, 1);

        private final String id;
        private final int repeatTicks;
        private final float aethumDrain;

        Mode(String id, int repeatTicks, float aethumDrain) {
            this.id = id;
            this.repeatTicks = repeatTicks;
            this.aethumDrain = aethumDrain;
        }

        public Mode next() {
            return Mode.values()[(this.ordinal() + 1) % Mode.values().length];
        }

        public static Mode byId(String id) {
            return switch (id) {
                default -> Mode.NORMAL;
                case "fast" -> Mode.FAST;
                case "ludicrous" -> Mode.LUDICROUS;
            };
        }
    }
}
