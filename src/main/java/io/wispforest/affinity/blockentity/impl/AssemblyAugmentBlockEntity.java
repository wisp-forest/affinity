package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.block.impl.ArcaneTreetapBlock;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.mixin.access.CraftingInventoryAccessor;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinityPoiTypes;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.util.ImplementedInventory;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class AssemblyAugmentBlockEntity extends BlockEntity implements TickedBlockEntity, ImplementedInventory, SidedInventory {

    private static final int[] DOWN_SLOTS = new int[]{0};
    private static final int[] NO_SLOTS = new int[0];

    private static final NbtKey<ItemStack> OUTPUT_KEY = new NbtKey<>("Output", NbtKey.Type.ITEM_STACK);

    private final Set<BlockPos> treetapCache = new HashSet<>();

    private final SimpleInventory craftingInput = new SimpleInventory(9);
    private final InputInventory craftingView = new InputInventory(this.craftingInput.stacks);
    private final SimpleInventory outputInventory = new SimpleInventory(1);

    private int craftingTick = 0;

    public AssemblyAugmentBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ASSEMBLY_AUGMENT, pos, state);
        this.craftingInput.addListener(sender -> this.markDirty());
    }

    @Override
    public void tickServer() {
        var currentRecipe = this.world.getRecipeManager().getFirstMatch(AffinityRecipeTypes.ASSEMBLY, this.craftingView, this.world);
        var outputStack = this.outputInventory.getStack(0);

        if (currentRecipe.isPresent() && ItemOps.canStack(outputStack, currentRecipe.get().getOutput())) {
            if (this.craftingTick++ > 40) {
                for (int i = 0; i < this.craftingInput.stacks.size(); i++) {
                    if (!ItemOps.emptyAwareDecrement(this.craftingInput.stacks.get(i))) {
                        this.craftingInput.stacks.set(i, ItemStack.EMPTY);
                    }
                }

                if (outputStack.isEmpty()) {
                    this.outputInventory.setStack(0, currentRecipe.get().getOutput().copy());
                } else {
                    outputStack.increment(currentRecipe.get().getOutput().getCount());
                }

                this.craftingTick = 0;
            }
        } else {
            this.craftingTick = 0;
        }

        if (this.world.getTime() % 20 != 0) return;

        this.treetapCache.clear();
        ((ServerWorld) this.world).getPointOfInterestStorage()
                .getInCircle(type -> type.value() == AffinityPoiTypes.ARCANE_TREETAP, this.pos, 10, PointOfInterestStorage.OccupationStatus.ANY)
                .map(PointOfInterest::getPos)
                .filter(poi -> ArcaneTreetapBlock.isProperlyAttached(this.world, poi))
                .forEach(this.treetapCache::add);

        AffinityParticleSystems.AFFINE_CANDLE_BREWING.spawn(this.world, Vec3d.ofCenter(this.pos),
                new AffinityParticleSystems.CandleData(this.treetapCache.stream().map(Vec3d::ofCenter).toList())
        );
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.craftingInput.clear();
        Inventories.readNbt(nbt, this.craftingInput.stacks);

        this.outputInventory.setStack(0, nbt.get(OUTPUT_KEY));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        Inventories.writeNbt(nbt, this.craftingInput.stacks);
        nbt.put(OUTPUT_KEY, this.outputInventory.getStack(0));
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.outputInventory.stacks;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return side == Direction.DOWN ? DOWN_SLOTS : NO_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public void markDirty() {
        if (this.world == null) return;
        this.world.markDirty(this.pos);
    }

    public Set<BlockPos> treetapCache() {
        return this.treetapCache;
    }

    public SimpleInventory craftingInput() {
        return this.craftingInput;
    }

    public int craftingTick() {
        return this.craftingTick;
    }

    public SimpleInventory outputInventory() {
        return this.outputInventory;
    }

    static {
        ItemStorage.SIDED.registerFallback((tableWorld, tablePos, state, blockEntity, context) -> {
            if (tableWorld.getBlockState(tablePos).isOf(Blocks.CRAFTING_TABLE) && tableWorld.getBlockEntity(tablePos.up()) instanceof AssemblyAugmentBlockEntity augment) {
                return InventoryStorage.of(augment, Direction.DOWN);
            } else {
                return null;
            }
        });
    }

    private static final class InputInventory extends CraftingInventory {

        @SuppressWarnings("ConstantConditions")
        public InputInventory(DefaultedList<ItemStack> backingList) {
            super(null, 3, 3);
            ((CraftingInventoryAccessor) (Object) this).affinity$setStacks(backingList);
        }

    }
}
