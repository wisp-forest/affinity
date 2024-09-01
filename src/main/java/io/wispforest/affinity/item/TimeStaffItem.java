package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.impl.StaffPedestalBlockEntity;
import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.client.render.InWorldTooltipProvider;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.endec.Endec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TimeStaffItem extends StaffItem implements DirectInteractionHandler {

    private static final InquirableOutlineProvider.Outline AOE = InquirableOutlineProvider.Outline.symmetrical(2, 1, 2);

    public static final ComponentType<Mode> MODE = Affinity.component("time_staff_mode", Mode.ENDEC);

    public static final TagKey<Block> IMMUNE_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Affinity.id("time_staff_immune"));

    public TimeStaffItem() {
        super(AffinityItems.settings().maxCount(1).rarity(Rarity.EPIC));
    }

    @Override
    protected float getAethumConsumption(ItemStack stack) {
        return stack.getOrDefault(MODE, Mode.NORMAL).aethumDrain;
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
    public void appendTooltipEntries(World world, BlockPos pos, StaffPedestalBlockEntity pedestal, List<InWorldTooltipProvider.Entry> entries) {
        entries.add(InWorldTooltipProvider.Entry.icon(this.getModeName(pedestal.getItem()), 8, 0));
    }

    @Override
    public ActionResult onPedestalScrolled(World world, BlockPos pos, StaffPedestalBlockEntity pedestal, boolean direction) {
        if (!world.isClient) {
            pedestal.getItem().apply(MODE, Mode.NORMAL, mode -> mode.cycle(direction));
            pedestal.markDirty();
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void pedestalTickServer(ServerWorld world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        var mode = pedestal.getItem().getOrDefault(MODE, Mode.NORMAL);
        var costPerBlock = (long) (mode.aethumDrain * 100);

        for (var targetPos : BlockPos.iterate(pos.add(-2, -1, -2), pos.add(2, 1, 2))) {
            if (targetPos.equals(pos)) continue;

            if (!pedestal.hasFlux(costPerBlock)) {
                return;
            }

            if (this.accelerate(world, targetPos, mode.repeatTicks)) {
                pedestal.consumeFlux(costPerBlock);
            }
        }
    }

    @Override
    public InquirableOutlineProvider.Outline getAreaOfEffect(World world, BlockPos pos, StaffPedestalBlockEntity pedestal) {
        return AOE;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking()) {
            var stack = user.getStackInHand(hand);
            if (!world.isClient) stack.apply(MODE, Mode.NORMAL, Mode::next);
            return TypedActionResult.success(stack, world.isClient);
        }

        return super.use(world, user, hand);
    }

    @Override
    protected TypedActionResult<ItemStack> executeSpell(World world, PlayerEntity player, ItemStack stack, int remainingTicks, @Nullable BlockPos clickedBlock) {
        if (world.isClient) return TypedActionResult.consume(stack);

        var res = (BlockHitResult) player.raycast(5, 0, false);

        if (world.random.nextInt(4) == 0) {
            AffinityParticleSystems.TIME_STAFF_ACCELERATE.spawn(world, player.getPos().add(0, 1.25, 0), res.getBlockPos());
        }

        var mode = stack.getOrDefault(MODE, Mode.NORMAL);
        this.accelerate(world, res.getBlockPos(), mode.repeatTicks);

        return TypedActionResult.consume(stack);
    }

    protected boolean accelerate(World world, BlockPos pos, int ticks) {
        boolean ticked = false;

        for (int i = 0; i < ticks; i++) {
            BlockState state = world.getBlockState(pos);
            BlockEntity be = world.getBlockEntity(pos);

            if (state.isIn(IMMUNE_BLOCKS)) return ticked;
            if (be instanceof StaffPedestalBlockEntity pedestal && pedestal.getItem().isOf(this)) return ticked;

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
        var mode = stack.getOrDefault(MODE, Mode.NORMAL);
        return Text.translatable(this.getTranslationKey() + ".mode." + mode.id, mode.repeatTicks + 1);
    }

    @Override
    public boolean shouldHandleInteraction(ItemStack stack, World world, BlockPos pos, BlockState state) {
        return !state.isOf(AffinityBlocks.STAFF_PEDESTAL);
    }

    public enum Mode {
        NORMAL("normal", 1, 0.1f),
        FAST("fast", 3, 0.4f),
        LUDICROUS("ludicrous", 7, 1);

        public static final Endec<Mode> ENDEC = Endec.STRING.xmap(id -> switch (id) {
            default -> Mode.NORMAL;
            case "fast" -> Mode.FAST;
            case "ludicrous" -> Mode.LUDICROUS;
        }, mode -> mode.id);

        private final String id;
        private final int repeatTicks;
        private final float aethumDrain;

        Mode(String id, int repeatTicks, float aethumDrain) {
            this.id = id;
            this.repeatTicks = repeatTicks;
            this.aethumDrain = aethumDrain;
        }

        public Mode next() {
            return this.cycle(true);
        }

        public Mode cycle(boolean direction) {
            int idx = this.ordinal() + (direction ? 1 : -1);
            if (idx < 0) idx += Mode.values().length;
            if (idx > Mode.values().length - 1) idx -= Mode.values().length;

            return Mode.values()[idx];
        }
    }
}
