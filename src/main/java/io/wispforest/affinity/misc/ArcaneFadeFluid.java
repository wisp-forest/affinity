package io.wispforest.affinity.misc;

import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundEvents;
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

    public static final NbtKey<Boolean> REMOVE_ENCHANTMENT_GLINT_KEY = new NbtKey<>("affinity:remove_enchantment_glint", NbtKey.Type.BOOLEAN);

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
            if (!(entity instanceof ItemEntity item)) return;

            var items = item.world.getEntitiesByClass(ItemEntity.class, item.getBoundingBox().expand(.75), ItemEntity::isAlive);

            if (tryCraft(items, input -> input.getStack().hasGlint(), catalyst -> catalyst.getStack().isOf(AffinityItems.DRAGON_DROP), (input, catalyst) -> {
                if (!ItemOps.emptyAwareDecrement(catalyst.getStack())) catalyst.discard();
                input.getStack().put(REMOVE_ENCHANTMENT_GLINT_KEY, true);
            })) return;

            if (tryCraft(items, input -> input.getStack().getRepairCost() != 0, catalyst -> catalyst.getStack().isOf(Items.POTION) && PotionUtil.getPotion(catalyst.getStack()) == Potions.HEALING, (input, catalyst) -> {
                if (ItemOps.emptyAwareDecrement(catalyst.getStack())) {
                    catalyst.world.spawnEntity(new ItemEntity(catalyst.world, catalyst.getX(), catalyst.getY(), catalyst.getZ(), Items.GLASS_BOTTLE.getDefaultStack()));
                } else {
                    catalyst.setStack(Items.GLASS_BOTTLE.getDefaultStack());
                }

                input.getStack().setRepairCost(0);
            })) return;
        });

        ENTITY_TICK_IN_FADE_EVENT.register(entity -> {
            if (!(entity instanceof LivingEntity living)) return;
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));
        });
    }

    private static void bleachSheep(Entity entity) {
        if (!(entity instanceof SheepEntity sheep) || sheep.getColor() == DyeColor.WHITE) return;

        sheep.setColor(DyeColor.WHITE);
        sheep.playSound(SoundEvents.ENTITY_EVOKER_CAST_SPELL, 1f, 1f);

        ClientParticles.setParticleCount(15);
        ClientParticles.spawn(ParticleTypes.WITCH, sheep.world, sheep.getEyePos(), 1f);
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
            if (!catalystPredicate.test(item)) continue;

            catalyst = item;
            break;
        }

        if (catalyst == null) return false;

        craftFunction.accept(input, catalyst);
        return true;
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
    protected int getFlowSpeed(WorldView world) {
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
