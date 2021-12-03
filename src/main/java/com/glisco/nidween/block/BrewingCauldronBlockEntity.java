package com.glisco.nidween.block;

import com.glisco.nidween.registries.NidweenBlocks;
import com.glisco.nidween.util.potion.PotionMixture;
import com.glisco.nidween.util.recipe.PotionMixingRecipe;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.particles.ClientParticles;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BrewingCauldronBlockEntity extends BlockEntity {

    @NotNull
    private PotionMixture currentPotion = PotionMixture.EMPTY;
    private int fillLevel = 0;
    private int processTick = 0;

    private PotionMixingRecipe cachedRecipe = null;
    private BlockPos sporeBlossomPos = null;

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(5, ItemStack.EMPTY);

    public BrewingCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(NidweenBlocks.Entities.BREWING_CAULDRON, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        currentPotion = PotionMixture.fromNbt(nbt.getCompound("PotionMixture"));
        fillLevel = nbt.getInt("FillLevel");
        processTick = nbt.getInt("ProcessTick");

        items.clear();
        Inventories.readNbt(nbt, items);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.put("PotionMixture", currentPotion.toNbt());
        nbt.putInt("FillLevel", fillLevel);
        nbt.putInt("ProcessTick", processTick);
        Inventories.writeNbt(nbt, items);
        super.writeNbt(nbt);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        WorldOps.updateIfOnServer(world, pos);
    }

    public float getFluidHeight() {
        return 0.3f + fillLevel * 0.2f;
    }

    public void tick() {
        if (!world.isClient()) {
            for (var item : world.getEntitiesByClass(ItemEntity.class, new Box(pos), itemEntity -> true)) {

                if (!canAddItem()) break;
                addItem(ItemOps.singleCopy(item.getStack()));
                if (!ItemOps.emptyAwareDecrement(item.getStack())) item.discard();
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1, 0.25f + world.random.nextFloat() * 0.5f);
            }
        }

        checkCanCraft();

        if (processTick > 0) {
            if (processTick < 100) {
                if (world.isClient) {
                    float r = (currentPotion.getColor() >> 16) / 255f;
                    float g = ((currentPotion.getColor() & 0xFF00) >> 8) / 255f;
                    float b = (currentPotion.getColor() & 0xFF) / 255f;

                    ClientParticles.setVelocity(new Vec3d(r, g, b));
                    ClientParticles.setParticleCount(2);
                    ClientParticles.spawnPrecise(ParticleTypes.ENTITY_EFFECT, world, Vec3d.of(pos).add(0.5, 0.8, 0.5), 0.6, 0.2, 0.6);

                    ParticleEffect dust = new DustParticleEffect(new Vec3f(233 / 255f, 100 / 255f, 178 / 255f), 1);
                    final var sporeBlossomOffset = sporeBlossomPos.subtract(pos).getY() + 1;

                    ClientParticles.setParticleCount(sporeBlossomOffset * 2);
                    ClientParticles.spawnLine(dust, world, Vec3d.of(pos).add(0.5, 0.8, 0.5), Vec3d.of(pos).add(0.5, sporeBlossomOffset, 0.5), 0.15f);

                    ClientParticles.spawn(ParticleTypes.FALLING_SPORE_BLOSSOM, world, Vec3d.of(pos).add(0.5, sporeBlossomOffset, 0.5), 0.5);
                }
                processTick++;
            } else {
                if (cachedRecipe == null) return;

                if (!world.isClient) {
                    world.playSound(null, pos, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 1, 1);

                    for (var ingredient : cachedRecipe.getItemInputs()) {
                        for (int i = 0; i < items.size(); i++) {
                            if (!ingredient.test(items.get(i))) continue;
                            items.set(i, ItemStack.EMPTY);
                            break;
                        }
                    }

                    this.currentPotion = new PotionMixture(cachedRecipe.getPotionOutput(), ImmutableList.of(), true);
                    markDirty();
                } else {
                    ClientParticles.setParticleCount(75);
                    ClientParticles.spawnPrecise(ParticleTypes.WITCH, world, Vec3d.of(pos).add(0.5, 0.9, 0.5), 0.75, 0.2, 0.75);
                }

                processTick = 0;
                cachedRecipe = null;
            }
        }

    }

    private void checkCanCraft() {

        if (sporeBlossomPos == null || !world.getBlockState(sporeBlossomPos).isOf(Blocks.SPORE_BLOSSOM)) {
            sporeBlossomPos = null;
            for (var pos : BlockPos.iterate(pos.add(0, 1, 0), pos.add(0, 4, 0))) {
                if (!world.getBlockState(pos).isOf(Blocks.SPORE_BLOSSOM)) continue;
                sporeBlossomPos = pos;
                break;
            }
        }

        this.cachedRecipe = PotionMixingRecipe.getMatching(world.getRecipeManager(), currentPotion, items).orElse(null);
        if (cachedRecipe == null || sporeBlossomPos == null) {
            this.processTick = 0;
        } else if (processTick == 0) {
            this.processTick = 1;
        }
    }

    public boolean canPotionBeExtracted() {
        return fillLevel > 0;
    }

    public ItemStack extractOneBottle() {
        if (!canPotionBeExtracted()) return ItemStack.EMPTY;

        final var currentPotionBackup = this.currentPotion;

        this.fillLevel--;
        if (fillLevel == 0) this.currentPotion = PotionMixture.EMPTY;
        this.markDirty();

        return currentPotionBackup.toStack();
    }

    public boolean canPotionBeAdded() {
        return fillLevel < 3;
    }

    public void addOneBottle(PotionMixture potion) {
        if (!canPotionBeAdded()) return;

        if (fillLevel == 0) {
            this.currentPotion = potion;
        } else {
            this.currentPotion = currentPotion.mix(potion);
        }

        this.fillLevel++;
        this.markDirty();
    }

    public void setCurrentPotion(@NotNull PotionMixture currentPotion) {
        this.currentPotion = currentPotion;
        this.markDirty();
    }

    public @NotNull PotionMixture getCurrentPotion() {
        return currentPotion;
    }

    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    public boolean canAddItem() {
        return items.contains(ItemStack.EMPTY);
    }

    public void addItem(ItemStack stack) {
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) continue;
            items.set(i, stack);
            break;
        }
        markDirty();
    }

    public boolean itemAvailable() {
        return items.stream().anyMatch(stack -> !stack.isEmpty());
    }

    public ItemStack getAndRemoveLast() {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i).isEmpty()) continue;

            final var stack = items.set(i, ItemStack.EMPTY);
            markDirty();

            return stack;
        }
        return ItemStack.EMPTY;
    }

    public static void tick(World world, BlockPos blockPos, BlockState state, BrewingCauldronBlockEntity blockEntity) {
        blockEntity.tick();
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        final var nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }
}
