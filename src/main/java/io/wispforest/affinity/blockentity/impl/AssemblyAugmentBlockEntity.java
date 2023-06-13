package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.block.impl.ArcaneTreetapBlock;
import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.blockentity.template.SyncedBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.item.CarbonCopyItem;
import io.wispforest.affinity.misc.util.BlockFinder;
import io.wispforest.affinity.mixin.access.CraftingInventoryAccessor;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinityPoiTypes;
import io.wispforest.affinity.object.AffinityRecipeTypes;
import io.wispforest.affinity.particle.GenericEmitterParticleEffect;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.util.ImplementedInventory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterest;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class AssemblyAugmentBlockEntity extends SyncedBlockEntity implements TickedBlockEntity, ImplementedInventory, SidedInventory, InquirableOutlineProvider {

    @Environment(EnvType.CLIENT)
    public double previewAngle = 0d;

    public static final int OUTPUT_SLOT = 9;
    private static final int[] ALL_SLOTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, OUTPUT_SLOT};

    private static final NbtKey<ItemStack> TEMPLATE_KEY = new NbtKey<>("Template", NbtKey.Type.ITEM_STACK);

    private static final Map<World, Map<BlockPos, BlockPos>> BLOCKED_TREETAPS_PER_WORLD = new WeakHashMap<>();
    private Map<BlockPos, BlockPos> blockedTreetaps;

    private CraftingRecipe autocraftingRecipe;

    private final Set<BlockPos> treetapCache = new HashSet<>();
    private int activeTreetaps = 0;

    private final SimpleInventory inventory = new SimpleInventory(10);
    private final InputInventory craftingView = new InputInventory(this.inventory.stacks);
    private final SimpleInventory templateInventory = new SimpleInventory(1);

    private int craftingTick = 0;

    public AssemblyAugmentBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ASSEMBLY_AUGMENT, pos, state);
        this.inventory.addListener(sender -> this.markDirty());
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.blockedTreetaps = BLOCKED_TREETAPS_PER_WORLD.computeIfAbsent(this.world, $ -> new HashMap<>());
    }

    @Override
    public void tickServer() {
        this.autocraftingRecipe = CarbonCopyItem.getRecipe(this.templateInventory.getStack(0), this.world);

        CraftingRecipe currentRecipe = null;
        if (this.autocraftingRecipe == null) {
            currentRecipe = this.world.getRecipeManager().getFirstMatch(AffinityRecipeTypes.ASSEMBLY, this.craftingView, this.world).orElse(null);
        } else if (this.autocraftingRecipe.matches(this.craftingView, this.world)) {
            currentRecipe = this.autocraftingRecipe;
        }

        var outputStack = this.inventory.getStack(OUTPUT_SLOT);

        this.activeTreetaps = 0;
        if (currentRecipe != null) {
            for (var treetap : this.treetapCache) {
                if (this.blockedTreetaps.containsKey(treetap) && !this.pos.equals(this.blockedTreetaps.get(treetap))) {
                    continue;
                }

                this.blockedTreetaps.put(treetap, this.pos);
                if (++this.activeTreetaps >= 5) break;
            }
        }

        if (this.activeTreetaps > 0 && ItemOps.canStack(outputStack, currentRecipe.getOutput(this.world.getRegistryManager()))) {
            var currentRecipeOutput = currentRecipe.getOutput(this.world.getRegistryManager());
            if (this.craftingTick % 20 == 0) {
                AffinityParticleSystems.BEZIER_VORTEX.spawn(this.world, Vec3d.ofCenter(this.pos, .2), new AffinityParticleSystems.BezierVortexData(
                        new GenericEmitterParticleEffect(
                                ArcaneTreetapBlock.PARTICLE, new Vec3d(.05, .05, .05),
                                1, .05f, true, 1
                        ),
                        this.treetapCache.stream().filter(blockPos -> this.pos.equals(this.blockedTreetaps.get(blockPos))).map(pos -> {
                            var direction = this.world.getBlockState(pos).get(ArcaneTreetapBlock.FACING);
                            return new Vec3d(pos.getX() + .5 + direction.getOffsetX() * .4, pos.getY() + .5, pos.getZ() + .5 + direction.getOffsetZ() * .4);
                        }).toList(), 1, 30, true
                ));
            }

            if (this.craftingTick++ > this.craftingDuration()) {
                for (int i = 0; i < 9; i++) {
                    if (!ItemOps.emptyAwareDecrement(this.inventory.stacks.get(i))) {
                        this.inventory.stacks.set(i, ItemStack.EMPTY);
                    }
                }

                if (outputStack.isEmpty()) {
                    this.inventory.setStack(OUTPUT_SLOT, currentRecipeOutput.copy());
                } else {
                    outputStack.increment(currentRecipeOutput.getCount());
                }

                this.markDirty();
                this.craftingTick = 0;
            }
        } else {
            this.activeTreetaps = 0;
            for (var treetap : this.treetapCache) {
                if (!this.getPos().equals(this.blockedTreetaps.get(treetap))) continue;
                this.blockedTreetaps.remove(treetap);
            }

            this.craftingTick = 0;
        }

        if (this.world.getTime() % 20 != 0) return;

        this.treetapCache.clear();
        var occupiedLogs = new HashSet<BlockPos>();

        BlockFinder.findPoi(this.world, AffinityPoiTypes.ARCANE_TREETAP, this.pos, 10)
                .map(PointOfInterest::getPos)
                .filter(poi -> {
                    if (!ArcaneTreetapBlock.isProperlyAttached(this.world, poi)) return false;

                    var tree = ArcaneTreetapBlock.walkConnectedTree(world, poi);
                    return tree.stream().allMatch(occupiedLogs::add);
                })
                .forEach(this.treetapCache::add);
    }

    public @Nullable CraftingRecipe autocraftingRecipe() {
        return this.autocraftingRecipe;
    }

    public Optional<CraftingRecipe> fetchActiveRecipe() {
        return this.world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, this.craftingView, this.world).or(() -> this.world.getRecipeManager().getFirstMatch(AffinityRecipeTypes.ASSEMBLY, this.craftingView, this.world));
    }

    @Override
    public @Nullable CuboidRenderer.Cuboid getActiveOutline() {
        return CuboidRenderer.Cuboid.of(
                new BlockPos(-10, -10, -10), new BlockPos(11, 11, 11)
        );
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.inventory.clear();
        Inventories.readNbt(nbt, this.inventory.stacks);

        this.templateInventory.setStack(0, nbt.get(TEMPLATE_KEY));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        Inventories.writeNbt(nbt, this.inventory.stacks);
        nbt.put(TEMPLATE_KEY, this.templateInventory.getStack(0));
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory.stacks;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == OUTPUT_SLOT || this.autocraftingRecipe == null || dir == Direction.UP) return false;

        int height, width = 1;
        outer:
        for (height = 1; height <= 3; height++) {
            for (width = 1; width <= 3; width++) {
                if (!this.autocraftingRecipe.fits(width, height)) continue;
                break outer;
            }
        }

        int x = slot % width, y = slot / width;
        if (x >= width || y >= height) return false;

        return this.inventory.getStack(slot).isEmpty() && this.autocraftingRecipe.getIngredients().get(y * width + x).test(stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == OUTPUT_SLOT;
    }

    public int displayTreetaps() {
        return (int) this.treetapCache.stream()
                .filter(blockPos -> !this.blockedTreetaps.containsKey(blockPos) || this.blockedTreetaps.get(blockPos) == this.pos)
                .count();
    }

    public SimpleInventory inventory() {
        return this.inventory;
    }

    public SimpleInventory templateInventory() {
        return this.templateInventory;
    }

    public int craftingTick() {
        return this.craftingTick;
    }

    public int craftingDuration() {
        return 205 - this.activeTreetaps * 40;
    }

    static {
        ItemStorage.SIDED.registerFallback((tableWorld, tablePos, state, blockEntity, context) -> {
            if (state.isOf(Blocks.CRAFTING_TABLE) && tableWorld.getBlockEntity(tablePos.up()) instanceof AssemblyAugmentBlockEntity augment) {
                return InventoryStorage.of(augment, context);
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
