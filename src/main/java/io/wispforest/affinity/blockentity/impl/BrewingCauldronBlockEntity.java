package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.misc.potion.PotionMixture;
import io.wispforest.affinity.misc.recipe.PotionMixingRecipe;
import io.wispforest.affinity.misc.util.ListUtil;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinityPoiTypes;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.particles.ClientParticles;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class BrewingCauldronBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {

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
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        storedPotion = PotionMixture.fromNbt(nbt.getCompound("PotionMixture"));
        fillLevel = nbt.getInt("FillLevel");
        processTick = nbt.getInt("ProcessTick");

        items.clear();
        Inventories.readNbt(nbt, items);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.put("PotionMixture", storedPotion.toNbt());
        nbt.putInt("FillLevel", fillLevel);
        nbt.putInt("ProcessTick", processTick);
        Inventories.writeNbt(nbt, items);
    }

    public float getFluidHeight() {
        return 0.3f + fillLevel * 0.2f;
    }

    public void tickClient() {
        updateCraftingPreconditions();

        if (processTick < 1) return;

        if (processTick < 100) {
            ClientParticles.setVelocity(MathUtil.splitRGBToVec3d(storedPotion.color()));
            ClientParticles.setParticleCount(2);
            ClientParticles.spawnPrecise(ParticleTypes.ENTITY_EFFECT, world, Vec3d.of(pos).add(0.5, 0.8, 0.5), 0.6, 0.2, 0.6);

            ParticleEffect dust = new DustParticleEffect(new Vec3f(233 / 255f, 100 / 255f, 178 / 255f), 1);
            final var sporeBlossomOffset = sporeBlossomPos.subtract(pos).getY() + 1;

            ClientParticles.setParticleCount(sporeBlossomOffset * 2);
            ClientParticles.spawnLine(dust, world, Vec3d.of(pos).add(0.5, 0.8, 0.5), Vec3d.of(pos).add(0.5, sporeBlossomOffset, 0.5), 0.15f);

            ClientParticles.spawn(ParticleTypes.FALLING_SPORE_BLOSSOM, world, Vec3d.of(pos).add(0.5, sporeBlossomOffset, 0.5), 0.5);
            processTick++;
        } else {
            ClientParticles.setParticleCount(75);
            ClientParticles.spawnPrecise(ParticleTypes.WITCH, world, Vec3d.of(pos).add(0.5, 0.9, 0.5), 0.75, 0.2, 0.75);

            this.processTick = 0;
            this.cachedRecipe = null;
        }
    }

    public void tickServer() {
        for (var item : world.getEntitiesByClass(ItemEntity.class, new Box(pos), itemEntity -> true)) {
            if (!canAddItem()) break;
            if (item.getScoreboardTags().contains("NoSuckySuckInCauldron")) continue;

            ListUtil.addItem(this.items, ItemOps.singleCopy(item.getStack()));
            this.markDirty();

            if (!ItemOps.emptyAwareDecrement(item.getStack())) item.discard();

            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1, 0.25f + world.random.nextFloat() * 0.5f);
        }

        updateCraftingPreconditions();

        if (processTick < 1) return;
        if (processTick++ < 100) {
            if (this.processTick % 20 == 0) {
                this.spawnCandleParticles();
            }

            return;
        }

        if (cachedRecipe == null) return;

        world.playSound(null, pos, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 1, 1);
        this.storedPotion = cachedRecipe.craftPotion(items);

        int affineCandleCount = this.countCandles();
        if (affineCandleCount > 0) {
            var extraNbt = this.storedPotion.getOrCreateExtraNbt();
            extraNbt.put(PotionMixture.EXTEND_DURATION_BY, 1 + Math.min(affineCandleCount * 0.05F, 0.45F));
        }

        for (var ingredient : cachedRecipe.getItemInputs()) {
            for (int i = 0; i < items.size(); i++) {
                if (!ingredient.test(items.get(i))) continue;
                items.set(i, ItemStack.EMPTY);
                break;
            }
        }

        this.markDirty(true);

        processTick = 0;
        cachedRecipe = null;
    }

    private void updateCraftingPreconditions() {
        this.cachedRecipe = PotionMixingRecipe.getMatching(world.getRecipeManager(), storedPotion, items).orElse(null);
        if (this.cachedRecipe == null) {
            this.processTick = 0;
            return;
        }

        if (sporeBlossomPos == null || !world.getBlockState(sporeBlossomPos).isOf(Blocks.SPORE_BLOSSOM)) {
            sporeBlossomPos = null;
            for (var pos : BlockPos.iterate(pos.add(0, 1, 0), pos.add(0, 4, 0))) {
                if (!world.getBlockState(pos).isOf(Blocks.SPORE_BLOSSOM)) continue;
                sporeBlossomPos = pos;
                break;
            }
        }

        if (this.sporeBlossomPos == null) {
            this.processTick = 0;
            return;
        }

        if (processTick != 0) return;

        this.spawnCandleParticles();
        this.processTick = 1;
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

        AffinityParticleSystems.AFFINE_CANDLE_BREWING.spawn(
                world,
                Vec3d.ofCenter(this.pos),
                new AffinityParticleSystems.CandleData(particleOrigins)
        );
    }

    private Stream<Candle> getCandles() {
        return ((ServerWorld) this.world).getPointOfInterestStorage()
                .getInCircle(type -> type.value() == AffinityPoiTypes.AFFINE_CANDLE, this.pos, 5, PointOfInterestStorage.OccupationStatus.ANY)
                .map(poi -> new Candle(poi.getPos(), world.getBlockState(poi.getPos())))
                .filter(candle -> candle.state.get(Properties.LIT));
    }

    public ItemStack extractOneBottle() {
        if (!canPotionBeExtracted()) return ItemStack.EMPTY;

        final var potionStack = this.storedPotion.toStack();

        this.fillLevel--;
        if (fillLevel == 0) this.storedPotion = PotionMixture.EMPTY;
        this.markDirty(true);

        return potionStack;
    }

    public void addOneBottle(PotionMixture potion) {
        if (!canPotionBeAdded()) return;

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

    @NotNull
    public PotionMixture storedPotion() {
        return storedPotion;
    }

    public boolean canAddItem() {
        return items.contains(ItemStack.EMPTY);
    }

    public boolean itemAvailable() {
        return items.stream().anyMatch(stack -> !stack.isEmpty());
    }

    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    private record Candle(BlockPos pos, BlockState state) {}
}
