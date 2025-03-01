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
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.serialization.RegistriesAttribute;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AssemblyAugmentBlockEntity extends SyncedBlockEntity implements TickedBlockEntity, InquirableOutlineProvider {

    @Environment(EnvType.CLIENT)
    public double previewAngle;

    public static final int OUTPUT_SLOT = 9;

    private static final KeyedEndec<ItemStack> TEMPLATE_KEY = MinecraftEndecs.ITEM_STACK.keyed("Template", ItemStack.EMPTY);

    private static final Map<World, Map<BlockPos, BlockPos>> BLOCKED_TREETAPS_PER_WORLD = new WeakHashMap<>();
    private Map<BlockPos, BlockPos> blockedTreetaps;

    @Nullable
    private RecipeEntry<CraftingRecipe> autocraftingRecipe;

    private final Set<BlockPos> treetapCache = new HashSet<>();
    private int activeTreetaps = 0;

    private final SimpleInventory inventory = new SimpleInventory(10);
    private final InputInventory craftingView = new InputInventory(this.inventory.heldStacks);
    private final SimpleInventory templateInventory = new SimpleInventory(1);
    private final Storage<ItemVariant> storage;

    private int craftingTick = 0;

    public AssemblyAugmentBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ASSEMBLY_AUGMENT, pos, state);
        this.inventory.addListener(sender -> this.markDirty());

        var inventoryStorage = InventoryStorage.of(AssemblyAugmentBlockEntity.this.inventory, null);
        var parts = new ArrayList<SingleSlotStorage<ItemVariant>>();
        for (int i = 0; i < 9; i++) {
            var slot = inventoryStorage.getSlot(i);
            parts.add(new SingleSlotStorage<>() {
                @Override
                public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {return slot.insert(resource, maxAmount, transaction);}
                @Override
                public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {return 0;}
                @Override
                public boolean isResourceBlank() {return slot.isResourceBlank();}
                @Override
                public ItemVariant getResource() {return slot.getResource();}
                @Override
                public long getAmount() {return slot.getAmount();}
                @Override
                public long getCapacity() {return slot.getCapacity();}
            });
        }
        parts.add(inventoryStorage.getSlot(OUTPUT_SLOT));

        this.storage = new CombinedStorage<>(parts) {
            @Override
            public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                if (AssemblyAugmentBlockEntity.this.autocraftingRecipe == null) return 0;
                var recipe = AssemblyAugmentBlockEntity.this.autocraftingRecipe.value();

                int height, width = 1;
                outer:
                for (height = 1; height <= 3; height++) {
                    for (width = 1; width <= 3; width++) {
                        if (!recipe.fits(width, height)) continue;
                        break outer;
                    }
                }

                var candidateSlots = new ArrayList<SingleSlotStorage<ItemVariant>>();

                var ingredients = recipe.getIngredients();
                for (int slot = 0; slot < ingredients.size(); slot++) {
                    int x = slot % 3, y = slot / 3;
                    if (x >= width || y >= height) continue;

                    if (ingredients.get(slot).test(resource.toStack())) candidateSlots.add(inventoryStorage.getSlot(slot));
                }

                if (candidateSlots.isEmpty()) return 0;
                candidateSlots.sort(Comparator.comparingLong(StorageView::getAmount));

                long remaining = maxAmount;

                if (candidateSlots.size() != 1 && candidateSlots.get(0).getAmount() < candidateSlots.get(candidateSlots.size() - 1).getAmount()) {
                    for (var slot : candidateSlots) {
                        var insertCount = candidateSlots.get(candidateSlots.size() - 1).getAmount() - slot.getAmount();
                        remaining -= slot.insert(resource, Math.min(insertCount, remaining), transaction);
                    }
                }

                for (int i = 0; i < candidateSlots.size(); i++) {
                    var insertCount = (int) Math.ceil((remaining) / (double) (candidateSlots.size() - i));
                    remaining -= candidateSlots.get(i).insert(resource, Math.min(insertCount, remaining), transaction);
                }

                return maxAmount - remaining;
            }
        };
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.blockedTreetaps = BLOCKED_TREETAPS_PER_WORLD.computeIfAbsent(this.world, $ -> new HashMap<>());
    }

    @Override
    public void tickServer() {
        this.autocraftingRecipe = CarbonCopyItem.getRecipe(this.templateInventory.getStack(0), this.world);

        // TODO: investigate whether it makes sense to not use CraftingInventory here
        var input = this.craftingView.createRecipeInput();

        RecipeEntry<CraftingRecipe> currentRecipe = null;
        if (this.autocraftingRecipe == null) {
            currentRecipe = this.world.getRecipeManager().getFirstMatch(AffinityRecipeTypes.ASSEMBLY, input, this.world).orElse(null);
        } else if (this.autocraftingRecipe.value().matches(input, this.world)) {
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

        var currentRecipeResult = this.activeTreetaps > 0 ? currentRecipe.value().craft(input, this.world.getRegistryManager()) : null;
        if (this.activeTreetaps > 0 && ItemOps.canStack(outputStack, currentRecipeResult)) {
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
                    if (!ItemOps.emptyAwareDecrement(this.inventory.heldStacks.get(i))) {
                        this.inventory.heldStacks.set(i, ItemStack.EMPTY);
                    }
                }

                if (outputStack.isEmpty()) {
                    this.inventory.setStack(OUTPUT_SLOT, currentRecipeResult);
                } else {
                    outputStack.increment(currentRecipeResult.getCount());
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

    public @Nullable RecipeEntry<CraftingRecipe> autocraftingRecipe() {
        return this.autocraftingRecipe;
    }

    public Optional<RecipeEntry<CraftingRecipe>> fetchActiveRecipe() {
        // TODO: investigate whether it makes sense to not use CraftingInventory here
        return this.world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, this.craftingView.createRecipeInput(), this.world).or(() -> this.world.getRecipeManager().getFirstMatch(AffinityRecipeTypes.ASSEMBLY, this.craftingView.createRecipeInput(), this.world));
    }

    @Override
    public @Nullable CuboidRenderer.Cuboid getActiveOutline() {
        return CuboidRenderer.Cuboid.of(
                new BlockPos(-10, -10, -10), new BlockPos(11, 11, 11)
        );
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        this.inventory.clear();
        Inventories.readNbt(nbt, this.inventory.heldStacks, registries);

        this.templateInventory.setStack(0, nbt.get(TEMPLATE_KEY));
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        Inventories.writeNbt(nbt, this.inventory.heldStacks, registries);
        nbt.put(SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) registries)), TEMPLATE_KEY, this.templateInventory.getStack(0));
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
        ItemStorage.SIDED.registerForBlockEntity((augment, direction) -> augment.storage, AffinityBlocks.Entities.ASSEMBLY_AUGMENT);
        ItemStorage.SIDED.registerFallback((tableWorld, tablePos, state, blockEntity, context) -> {
            if (state.isIn(ConventionalBlockTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES) && tableWorld.getBlockEntity(tablePos.up()) instanceof AssemblyAugmentBlockEntity augment) {
                return augment.storage;
            } else {
                return null;
            }
        });
    }

    private static final class InputInventory extends CraftingInventory {

        @SuppressWarnings("ConstantConditions")
        public InputInventory(DefaultedList<ItemStack> backingList) {
            super(null, 3, 3);
            ((CraftingInventoryAccessor) (Object) this).affinity$setStacks(new DefaultedList<>(backingList.subList(0, backingList.size() - 1), ItemStack.EMPTY) {});
        }
    }
}
