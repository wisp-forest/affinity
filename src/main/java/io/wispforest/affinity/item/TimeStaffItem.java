package io.wispforest.affinity.item;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class TimeStaffItem extends Item implements DirectInteractionHandler {
    public static final NbtKey<Mode> MODE = new NbtKey<>("Mode", NbtKey.Type.STRING.then(Mode::byId, mode -> mode.id));

    public TimeStaffItem() {
        super(AffinityItems.settings(AffinityItemGroup.MAIN).maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking()) {
            var stack = user.getStackInHand(hand);

            stack.mutate(MODE, Mode::next);

            return TypedActionResult.success(stack, world.isClient);
        }

        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        Mode mode = stack.get(MODE);

        var aethum = AffinityComponents.PLAYER_AETHUM.get(user);
        if (aethum.getAethum() < mode.aethumDrain) return;

        aethum.setAethum(aethum.getAethum() - mode.aethumDrain);

        if (world.isClient) return;

        var res = (BlockHitResult) user.raycast(5, 0, false);

        if (world.random.nextInt(4) == 0)
            AffinityParticleSystems.WISP_ATTACK.spawn(world, user.getEyePos(), new AffinityParticleSystems.LineData(
                Vec3d.ofCenter(res.getBlockPos()), 0xFFFFFF
            ));

        for (int i = 0; i < mode.repeatTicks; i++) {
            BlockState state = world.getBlockState(res.getBlockPos());
            BlockEntity be = world.getBlockEntity(res.getBlockPos());

            if (be != null) {
                BlockEntityTicker<BlockEntity> ticker = state.getBlockEntityTicker(world, (BlockEntityType<BlockEntity>) be.getType());

                if (ticker != null) {
                      ticker.tick(world, res.getBlockPos(), state, be);
                }
            }

            if (state.hasRandomTicks()) {
                int randomTickPeriod = 4096 / world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);

                if (world.random.nextInt(randomTickPeriod) == 0) {
                    state.randomTick((ServerWorld) world, res.getBlockPos(), world.random);
                }
            }
        }
    }

    @Override
    public boolean shouldHandleInteraction(World world, BlockPos pos, BlockState state) {
        return true;
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
