package io.wispforest.affinity.item;

import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TimeStaffItem extends Item implements DirectInteractionHandler {

    public static final NbtKey<Mode> MODE = new NbtKey<>("Mode", NbtKey.Type.STRING.then(Mode::byId, mode -> mode.id));

    public TimeStaffItem() {
        super(AffinityItems.settings(AffinityItemGroup.MAIN).maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking()) {
            var stack = user.getStackInHand(hand);
            if (!world.isClient) stack.mutate(MODE, Mode::next);
            return TypedActionResult.success(stack, world.isClient);
        }

        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public Text getName(ItemStack stack) {
        var mode = stack.get(MODE);
        return Text.translatable(this.getTranslationKey()).append(Text.translatable(
                this.getTranslationKey() + ".mode_suffix",
                Text.translatable(this.getTranslationKey() + ".mode." + mode.id, mode.repeatTicks + 1)
        ));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        final var mode = stack.get(MODE);
        tooltip.add(Text.translatable(
                this.getTranslationKey() + ".tooltip",
                Text.translatable(this.getTranslationKey() + ".mode." + mode.id, mode.repeatTicks + 1)
        ));
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

        if (world.random.nextInt(7) == 0)
            AffinityParticleSystems.TIME_STAFF_ACCELERATE.spawn(world, user.getPos().add(0, 1.25, 0), res.getBlockPos());

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
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
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
