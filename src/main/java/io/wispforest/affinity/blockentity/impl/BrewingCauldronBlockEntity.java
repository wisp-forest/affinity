package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.AffineCandleBlock;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.component.EntityFlagComponent;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.misc.util.BlockFinder;
import io.wispforest.affinity.misc.util.ListUtil;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.*;
import io.wispforest.affinity.recipe.PotionMixingRecipe;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.serialization.RegistriesAttribute;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.stream.Stream;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class BrewingCauldronBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {

    public static final KeyedEndec<Integer> FILL_LEVEL_KEY = Endec.INT.keyed("FillLevel", 0);
    public static final KeyedEndec<Integer> PROCESS_TICK_KEY = Endec.INT.keyed("ProcessTick", 0);
    public static final KeyedEndec<PotionMixture> STORED_POTION_KEY = PotionMixture.ENDEC.keyed("PotionMixture", PotionMixture.EMPTY);

    @NotNull
    private PotionMixture storedPotion = PotionMixture.EMPTY;
    private int fillLevel = 0;
    private int processTick = 0;

    private PotionMixingRecipe cachedRecipe = null;
    private BlockPos sporeBlossomPos = null;

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(5, ItemStack.EMPTY);

    public BrewingCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.BREWING_CAULDRON, pos, state);

        this.fluxStorage.setFluxCapacity(64000);
        this.fluxStorage.setMaxInsert(64);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        this.storedPotion = nbt.get(STORED_POTION_KEY);
        this.fillLevel = nbt.get(FILL_LEVEL_KEY);
        this.processTick = nbt.get(PROCESS_TICK_KEY);

        this.items.clear();
        Inventories.readNbt(nbt, this.items, registries);
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        var ctx = SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) registries));
        nbt.put(ctx, STORED_POTION_KEY, this.storedPotion);
        nbt.put(ctx, FILL_LEVEL_KEY, this.fillLevel);
        nbt.put(ctx, PROCESS_TICK_KEY, this.processTick);
        Inventories.writeNbt(nbt, this.items, registries);
    }

    @Override
    public void tickClient() {
        this.updateAndTestCraftingPreconditions();

        if (this.processTick < 1) return;

        if (this.processTick < 100) {
            ClientParticles.setVelocity(MathUtil.rgbToVec3d(storedPotion.color()));
            ClientParticles.setParticleCount(2);
            ClientParticles.spawnPrecise(
                    EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, Affinity.AETHUM_FLUX_COLOR.rgb()),
                    world, Vec3d.of(pos).add(0.5, 0.8, 0.5),
                    0.6, 0.2, 0.6
            );

            ParticleEffect dust = new DustParticleEffect(new Vector3f(233 / 255f, 100 / 255f, 178 / 255f), 1);
            final var sporeBlossomOffset = sporeBlossomPos.subtract(pos).getY() + 1;

            ClientParticles.setParticleCount(sporeBlossomOffset * 2);
            ClientParticles.spawnLine(dust, world, Vec3d.of(pos).add(0.5, 0.8, 0.5), Vec3d.of(pos).add(0.5, sporeBlossomOffset, 0.5), 0.15f);

            ClientParticles.spawn(ParticleTypes.FALLING_SPORE_BLOSSOM, world, Vec3d.of(pos).add(0.5, sporeBlossomOffset, 0.5), 0.5);
            this.processTick++;
        } else {
            ClientParticles.setParticleCount(75);
            ClientParticles.spawnPrecise(ParticleTypes.WITCH, world, Vec3d.of(pos).add(0.5, 0.9, 0.5), 0.75, 0.2, 0.75);

            this.processTick = 0;
            this.cachedRecipe = null;
        }
    }

    @Override
    public void tickServer() {
        for (var item : world.getEntitiesByClass(ItemEntity.class, new Box(pos), itemEntity -> true)) {
            if (!this.canAddItem()) break;
            if (item.getComponent(AffinityComponents.ENTITY_FLAGS).hasFlag(EntityFlagComponent.SPAWNED_BY_BREWING_CAULDRON)) {
                continue;
            }

            ListUtil.addItem(this.items, ItemOps.singleCopy(item.getStack()));
            this.markDirty();

            if (!ItemOps.emptyAwareDecrement(item.getStack())) item.discard();

            world.playSound(null, pos, AffinitySoundEvents.BLOCK_BREWING_CAULDRON_PICK_UP_ITEM, SoundCategory.BLOCKS, 1, 0.25f + world.random.nextFloat() * 0.5f);
        }

        if (!this.updateAndTestCraftingPreconditions()) {
            this.processTick = 0;
            return;
        }

        this.updateFlux(this.flux() - this.craftingFluxCost());
        if (this.processTick++ < 100) {
            if (this.processTick % 20 == 0 || this.processTick == 2) {
                this.spawnCandleParticles();
            }

            return;
        }

        world.playSound(null, this.pos, AffinitySoundEvents.BLOCK_BREWING_CAULDRON_BREW, SoundCategory.BLOCKS, 1, 1);
        this.storedPotion = this.cachedRecipe.craftPotion(this.items);

        int affineCandleCount = this.countCandles();
        if (affineCandleCount > 0) {
            var extraNbt = this.storedPotion.extraComponents();
            extraNbt.set(PotionMixture.EXTEND_DURATION_BY, 1 + Math.min(affineCandleCount * 0.05F, 0.5F));
        }

        for (var ingredient : this.cachedRecipe.itemInputs) {
            for (int i = 0; i < this.items.size(); i++) {
                if (!ingredient.test(this.items.get(i))) continue;
                this.items.set(i, ItemStack.EMPTY);
                break;
            }
        }

        this.markDirty(true);

        this.processTick = 0;
        this.cachedRecipe = null;
    }

    private boolean updateAndTestCraftingPreconditions() {
        var input = new PotionMixingRecipe.Input(this.items, this.storedPotion);
        this.cachedRecipe = this.world.getRecipeManager().getFirstMatch(AffinityRecipeTypes.POTION_MIXING, input, this.world).map(RecipeEntry::value).orElse(null);
        if (this.cachedRecipe == null) return false;

        if (this.sporeBlossomPos == null || !this.world.getBlockState(this.sporeBlossomPos).isOf(Blocks.SPORE_BLOSSOM)) {
            this.sporeBlossomPos = null;

            for (var pos : BlockPos.iterate(pos.add(0, 1, 0), pos.add(0, 4, 0))) {
                if (!this.world.getBlockState(pos).isOf(Blocks.SPORE_BLOSSOM)) continue;

                this.sporeBlossomPos = pos;
                break;
            }
        }

        if (this.sporeBlossomPos == null) return false;
        if (this.flux() < this.craftingFluxCost()) return false;

        if (this.processTick == 0) {
            this.processTick = 1;
        }

        return true;
    }

    private int craftingFluxCost() {
        return this.cachedRecipe == null || !this.cachedRecipe.strong ? 20 : 40;
    }

    private int countCandles() {
        return this.getCandles().mapToInt(value -> value.state.get(CandleBlock.CANDLES)).sum();
    }

    private void spawnCandleParticles() {
        var particleOrigins = this.getCandles()
                .map(candle -> Vec3d.of(candle.pos)
                        .add(Util.getRandom(
                                CandleBlock.CANDLES_TO_PARTICLE_OFFSETS.get(candle.state.get(CandleBlock.CANDLES)),
                                this.world.random
                        ))
                ).toList();

        AffinityParticleSystems.BEZIER_VORTEX.spawn(
                this.world,
                Vec3d.ofCenter(this.pos),
                new AffinityParticleSystems.BezierVortexData(ParticleTypes.REVERSE_PORTAL, particleOrigins, 20, 30, false)
        );
    }

    private Stream<Candle> getCandles() {
        return BlockFinder.findPoi(this.world, AffinityPoiTypes.AFFINE_CANDLE, this.pos, 5)
                .map(poi -> new Candle(poi.getPos(), this.world.getBlockState(poi.getPos())))
                .filter(candle -> candle.state.get(AffineCandleBlock.LIT));
    }

    public ItemStack extractOneBottle() {
        if (!this.canPotionBeExtracted()) return ItemStack.EMPTY;

        final var potionStack = this.storedPotion.toStack();

        this.fillLevel--;
        if (this.fillLevel == 0) this.storedPotion = PotionMixture.EMPTY;
        this.markDirty(true);

        return potionStack;
    }

    public void addOneBottle(PotionMixture potion) {
        if (!this.canPotionBeAdded()) return;

        if (fillLevel == 0) {
            this.storedPotion = potion;
        } else {
            this.storedPotion = storedPotion.mix(potion);
        }

        this.fillLevel++;
        this.markDirty(true);
    }

    public boolean canPotionBeExtracted() {
        return fillLevel > 0;
    }

    public boolean canPotionBeAdded() {
        return fillLevel < 3;
    }

    public float fluidHeight() {
        return 0.3f + fillLevel * 0.2f;
    }

    public float fillPercentage() {
        return fillLevel / 3f;
    }

    @NotNull
    public PotionMixture storedPotion() {
        return this.storedPotion;
    }

    public boolean canAddItem() {
        return this.items.contains(ItemStack.EMPTY);
    }

    public boolean itemAvailable() {
        return this.items.stream().anyMatch(stack -> !stack.isEmpty());
    }

    public DefaultedList<ItemStack> getItems() {
        return this.items;
    }

    private record Candle(BlockPos pos, BlockState state) {}
}
