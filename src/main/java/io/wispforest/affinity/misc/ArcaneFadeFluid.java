package io.wispforest.affinity.misc;

import io.wispforest.affinity.misc.potion.PotionUtil;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinitySoundEvents;
import io.wispforest.owo.ops.ItemOps;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.state.StateManager;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public abstract class ArcaneFadeFluid extends FlowableFluid {

    public static final Event<OnTouch> ENTITY_TOUCH_EVENT = EventFactory.createArrayBacked(OnTouch.class, listeners -> entity -> {
        for (var listener : listeners) {
            listener.onTouch(entity);
        }
    });

    public static final Event<OnTouch> ENTITY_TICK_IN_FADE_EVENT = EventFactory.createArrayBacked(OnTouch.class, listeners -> entity -> {
        for (var listener : listeners) {
            listener.onTouch(entity);
        }
    });

    static {
        ENTITY_TOUCH_EVENT.register(ArcaneFadeFluid::bleachSheep);
        ENTITY_TICK_IN_FADE_EVENT.register(ArcaneFadeFluid::bleachSheep);

        ENTITY_TOUCH_EVENT.register(entity -> {
            if (!(entity instanceof ItemEntity item) || item.getWorld().isClient) return;

            var items = item.getWorld().getEntitiesByClass(ItemEntity.class, item.getBoundingBox().expand(.75), ItemEntity::isAlive);

            if (tryCraft(items, input -> input.getStack().hasGlint(), catalyst -> catalyst.getStack().isOf(AffinityItems.DRAGON_DROP), (input, catalyst) -> {
                if (!ItemOps.emptyAwareDecrement(catalyst.getStack())) catalyst.discard();

                var output = input.getStack().copy();
                output.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);

                input.setStack(output);
            })) {return;}

            if (tryCraft(items, input -> input.getStack().getOrDefault(DataComponentTypes.REPAIR_COST, 0) != 0, catalyst -> catalyst.getStack().isOf(Items.POTION) && PotionUtil.getPotion(catalyst.getStack()) == Potions.HEALING, (input, catalyst) -> {
                if (ItemOps.emptyAwareDecrement(catalyst.getStack())) {
                    catalyst.getWorld().spawnEntity(new ItemEntity(catalyst.getWorld(), catalyst.getX(), catalyst.getY(), catalyst.getZ(), Items.GLASS_BOTTLE.getDefaultStack()));
                } else {
                    catalyst.setStack(Items.GLASS_BOTTLE.getDefaultStack());
                }

                input.getStack().set(DataComponentTypes.REPAIR_COST, 0);
            })) {return;}
        });

        ENTITY_TICK_IN_FADE_EVENT.register(entity -> {
            if (!(entity instanceof LivingEntity living)) return;
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));
        });
    }

    private static boolean tryCraft(List<ItemEntity> items, Predicate<ItemEntity> inputPredicate, Predicate<ItemEntity> catalystPredicate, BiConsumer<ItemEntity, ItemEntity> craftFunction) {
        ItemEntity input = null;
        for (var item : items) {
            if (!inputPredicate.test(item)) continue;

            input = item;
            break;
        }

        if (input == null) return false;

        ItemEntity catalyst = null;
        for (var item : items) {
            if (input == item || !catalystPredicate.test(item)) continue;

            catalyst = item;
            break;
        }

        if (catalyst == null) return false;

        craftFunction.accept(input, catalyst);

        input.playSound(AffinitySoundEvents.FLUID_ARCANE_FADE_CRAFT, 1f, input.getWorld().random.nextFloat() * 2f);
        AffinityParticleSystems.ARCANE_FADE_CRAFT.spawn(input.getWorld(), input.getPos().add(0, .5f, 0));

        return true;
    }

    private static void bleachSheep(Entity entity) {
        if (!(entity instanceof SheepEntity sheep) || sheep.getColor() == DyeColor.WHITE) return;

        sheep.setColor(DyeColor.WHITE);
        sheep.playSound(AffinitySoundEvents.FLUID_ARCANE_FADE_BLEACH, 1f, 1f);

        AffinityParticleSystems.ARCANE_FADE_BLEACH_SHEEP.spawn(sheep.getWorld(), MathUtil.entityCenterPos(sheep), 1f);
    }

    @Override
    public Fluid getFlowing() {
        return AffinityBlocks.Fluids.ARCANE_FADE_FLOWING;
    }

    @Override
    public Fluid getStill() {
        return AffinityBlocks.Fluids.ARCANE_FADE;
    }

    @Override
    protected boolean isInfinite(World world) {
        return false;
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
    }

    @Override
    protected int getMaxFlowDistance(WorldView world) {
        return 4;
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView world) {
        return 1;
    }

    @Override
    public Item getBucketItem() {
        return AffinityItems.ARCANE_FADE_BUCKET;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    public int getTickRate(WorldView world) {
        return 5;
    }

    @Override
    protected float getBlastResistance() {
        return 100f;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return AffinityBlocks.ARCANE_FADE.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == AffinityBlocks.Fluids.ARCANE_FADE || fluid == AffinityBlocks.Fluids.ARCANE_FADE_FLOWING;
    }

    public static class Still extends ArcaneFadeFluid {

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }

        @Override
        public int getLevel(FluidState state) {
            return 8;
        }
    }

    public static class Flowing extends ArcaneFadeFluid {

        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }
    }

    public interface OnTouch {
        void onTouch(Entity entity);
    }
}
