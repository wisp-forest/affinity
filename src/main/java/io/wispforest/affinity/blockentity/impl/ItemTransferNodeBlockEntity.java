package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.block.impl.ItemTransferNodeBlock;
import io.wispforest.affinity.blockentity.template.*;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.client.render.InWorldTooltipProvider;
import io.wispforest.affinity.item.PhantomBundleItem;
import io.wispforest.affinity.misc.callback.BeforeMangroveBasketCaptureCallback;
import io.wispforest.affinity.misc.screenhandler.ItemTransferNodeScreenHandler;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.serialization.RegistriesAttribute;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.util.VectorRandomUtils;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class ItemTransferNodeBlockEntity extends SyncedBlockEntity implements TickedBlockEntity, InWorldTooltipProvider, LinkableBlockEntity, InteractableBlockEntity, InquirableOutlineProvider, BeforeMangroveBasketCaptureCallback {

    public static final KeyedEndec<Set<BlockPos>> LINKS_KEY = MinecraftEndecs.BLOCK_POS.listOf().<Set<BlockPos>>xmap(HashSet::new, ArrayList::new).keyed("Links", HashSet::new);
    public static final KeyedEndec<List<ItemEntry>> ENTRIES_KEY = ItemEntry.ENDEC.listOf().keyed("Entries", ArrayList::new);

    public static final KeyedEndec<Mode> MODE_KEY = Mode.ENDEC.keyed("Mode", Mode.IDLE);
    public static final KeyedEndec<Integer> STACK_SIZE_KEY = Endec.INT.keyed("StackSize", 8);
    public static final KeyedEndec<ItemStack> FILTER_STACK_KEY = MinecraftEndecs.ITEM_STACK.keyed("FilterStack", ItemStack.EMPTY);

    public static final KeyedEndec<Boolean> IGNORE_DAMAGE_KEY = Endec.BOOLEAN.keyed("IgnoreDamage", true);
    public static final KeyedEndec<Boolean> IGNORE_DATA_KEY = Endec.BOOLEAN.keyed("IgnoreData", true);
    public static final KeyedEndec<Boolean> INVERT_FILTER_KEY = Endec.BOOLEAN.keyed("InvertFilter", false);

    private Set<BlockPos> links = new HashSet<>();
    private List<ItemEntry> entries = new ArrayList<>();

    private BlockApiCache<Storage<ItemVariant>, Direction> storageCache;
    private Direction facing;

    @NotNull private Mode mode = Mode.IDLE;
    private int stackSize = 8;

    public boolean ignoreDamage = true;
    public boolean ignoreData = true;
    public boolean invertFilter = false;
    @NotNull private ItemStack filterStack = ItemStack.EMPTY;

    private long time = ThreadLocalRandom.current().nextLong(0, 10);
    private int startIndex = 0;

    public ItemTransferNodeBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ITEM_TRANSFER_NODE, pos, state);
        this.facing = state.get(ItemTransferNodeBlock.FACING);
    }

    @Override
    public void setCachedState(BlockState state) {
        super.setCachedState(state);
        this.facing = state.get(ItemTransferNodeBlock.FACING);
    }

    @Override
    public Optional<String> beginLink(PlayerEntity player, NbtCompound linkData) {
        return Optional.empty();
    }

    @Override
    public Optional<LinkResult> finishLink(PlayerEntity player, BlockPos linkTo, NbtCompound linkData) {
        if (!(this.world.getBlockEntity(linkTo) instanceof ItemTransferNodeBlockEntity other)) {
            return Optional.of(LinkResult.NO_TARGET);
        }
        if (Math.abs(linkTo.getX() - this.pos.getX()) > 15 || Math.abs(linkTo.getY() - this.pos.getY()) > 15 || Math.abs(linkTo.getZ() - this.pos.getZ()) > 15) {
            return Optional.of(LinkResult.OUT_OF_RANGE);
        }

        if (!this.addLink(linkTo)) return Optional.of(LinkResult.ALREADY_LINKED);
        other.addLink(this.pos);

        return Optional.of(LinkResult.LINK_CREATED);
    }

    @Override
    public Optional<LinkResult> destroyLink(PlayerEntity player, BlockPos destroyFrom, NbtCompound linkData) {
        if (!this.removeLink(destroyFrom)) return Optional.of(LinkResult.NOT_LINKED);

        if (this.world.getBlockEntity(destroyFrom) instanceof ItemTransferNodeBlockEntity other) {
            other.removeLink(this.pos);
        }

        return Optional.of(LinkResult.LINK_CREATED);
    }

    private void clearLinks() {
        for (var link : this.links) {
            if (!(world.getBlockEntity(link) instanceof ItemTransferNodeBlockEntity node)) continue;
            node.removeLink(this.pos);
        }
    }

    public void onBroken() {
        this.clearLinks();
        for (var entry : this.entries) {
            if (!entry.insert) continue;
            this.dropItem(entry.item);
        }
    }

    private boolean addLink(BlockPos pos) {
        if (!this.links.add(pos)) return false;

        this.startIndex = 0;
        this.markDirty();

        return true;
    }

    private boolean removeLink(BlockPos pos) {
        if (!this.links.remove(pos)) return false;

        this.startIndex = 0;
        this.markDirty();

        return true;
    }

    @Override
    public boolean beforeMangroveBasketCapture(World world, BlockPos pos, MutableObject<BlockState> state, BlockEntity blockEntity) {
        this.clearLinks();
        return true;
    }

    @Override
    public void tickServer() {
        if (this.entries.removeIf(entry -> {
            if (++entry.age < 10) return false;
            if (!entry.insert) return true;

            var item = this.storageTransaction((storage, transaction) -> {
                int inserted = (int) storage.insert(ItemVariant.of(entry.item), entry.item.getCount(), transaction);
                transaction.commit();

                if (inserted != entry.item.getCount()) {
                    return entry.item.copyWithCount(entry.item.getCount() - inserted);
                } else {
                    return ItemStack.EMPTY;
                }
            });

            var origin = this.world.getBlockEntity(entry.originNode);
            if (origin instanceof ItemTransferNodeBlockEntity originNode && originNode != this) {
                if (item == null) {
                    this.dropItem(entry.item);
                } else if (!item.isEmpty()) {
                    this.sendToNode(originNode, item, item.getCount());
                }
            } else {
                if (item == null) {
                    this.dropItem(entry.item);
                } else if (!item.isEmpty()) {
                    this.dropItem(item);
                }
            }

            return true;
        })) {
            this.markDirty();
        }

        if (this.time++ % 10 != 0) return;
        if (this.world.getReceivedRedstonePower(this.pos) > 0) return;

        if (this.mode == Mode.SENDING) {
            if (!this.entries.isEmpty()) return;

            var targets = this.linkedNodes(mode -> mode != Mode.SENDING);

            if (targets.isEmpty()) return;

            this.startIndex = (this.startIndex + 1) % targets.size();
            var firstTarget = targets.get(this.startIndex);

            var stack = this.storageTransaction((storage, transaction) -> {
                Predicate<ItemVariant> predicate = item -> this.acceptsItem(item)
                    && firstTarget.acceptsItem(item)
                    && firstTarget.maxInsertCount(item, Transaction.getCurrentUnsafe()) > 0;

                var resource = StorageUtil.findExtractableResource(storage, predicate, transaction);
                if (resource == null) return ItemStack.EMPTY;

                int extracted = (int) storage.extract(resource, this.stackSize, transaction);
                transaction.commit();

                return resource.toStack(extracted);
            });

            if (stack == null || stack.isEmpty()) return;

            this.entries.add(new ItemEntry(this.pos, stack.copy(), 0, false));
            this.markDirty();

            if (!targets.isEmpty()) {
                var insertVariant = ItemVariant.of(stack);

                int validTargets = 0;
                for (var node : targets) {
                    if (node.acceptsItem(insertVariant)) validTargets++;
                }

                int countPerTarget = (int) Math.ceil(stack.getCount() / (double) validTargets);

                for (int i = this.startIndex; i < targets.size() + startIndex; i++) {
                    if (stack.isEmpty()) break;

                    var node = targets.get(i % targets.size());
                    if (!node.acceptsItem(insertVariant)) continue;

                    int insertCount = Math.min(node.maxInsertCount(insertVariant, null), Math.min(countPerTarget, stack.getCount()));
                    if (insertCount == 0) continue;

                    this.sendToNode(node, stack, insertCount);
                }
            }

            if (!stack.isEmpty()) this.insertItem(this, stack, 0);
        }
    }

    private void sendToNode(ItemTransferNodeBlockEntity targetNode, ItemStack stack, int insertCount) {
        var transferTime = Math.max(15, (int) Math.round(Math.sqrt(targetNode.pos.getSquaredDistance(this.pos))) * 5);
        AffinityParticleSystems.DISSOLVE_ITEM.spawn(this.world, particleOrigin(this), new AffinityParticleSystems.DissolveData(
            stack.copy(), particleOrigin(targetNode), 10, transferTime
        ));

        targetNode.insertItem(this, stack.copyWithCount(insertCount), transferTime + 10);
        stack.decrement(insertCount);
    }

    @Override
    public void tickClient() {
        this.entries.forEach(entry -> entry.age++);

        if (this.world.getReceivedRedstonePower(this.pos) > 0 && this.world.random.nextFloat() < .075f) {
            var pos = Vec3d.ofCenter(this.pos).add(
                this.facing.getOffsetX() * .3,
                this.facing.getOffsetY() * .3,
                this.facing.getOffsetZ() * .3

            );

            ClientParticles.setParticleCount(5);
            ClientParticles.spawn(new DustParticleEffect(DustParticleEffect.RED, .5f), this.world, pos, .25f);
        }
    }

    private void insertItem(ItemTransferNodeBlockEntity origin, ItemStack item, int delay) {
        this.entries.add(new ItemEntry(origin.pos, item, -delay, true));
        this.markDirty();
    }

    private @Nullable ItemStack storageTransaction(BiFunction<Storage<ItemVariant>, Transaction, @Nullable ItemStack> action) {
        var storage = this.attachedStorage();
        if (storage == null) return null;

        try (var transaction = Transaction.openOuter()) {
            return action.apply(storage, transaction);
        }
    }

    private @Nullable Storage<ItemVariant> attachedStorage() {
        if (this.storageCache == null) {
            this.storageCache = BlockApiCache.create(ItemStorage.SIDED, (ServerWorld) this.world, this.pos.offset(this.facing));
        }
        return this.storageCache.find(this.facing.getOpposite());
    }

    private void dropItem(ItemStack stack) {
        while (!stack.isEmpty()) {
            int dropCount = Math.min(stack.getCount(), stack.getMaxCount());
            var dropStack = stack.copyWithCount(dropCount);

            var velocity = VectorRandomUtils.getRandomOffset(this.world, Vec3d.ZERO, .05);
            this.world.spawnEntity(new ItemEntity(
                this.world,
                this.pos.getX() + .5 - this.facing.getOffsetX() * .15,
                this.pos.getY() + .5 - this.facing.getOffsetY() * .15,
                this.pos.getZ() + .5 - this.facing.getOffsetZ() * .15,
                dropStack, velocity.x, Math.abs(velocity.y) * 2, velocity.z
            ));

            stack.decrement(dropCount);
        }
    }

    private int maxInsertCount(ItemVariant variant, @Nullable TransactionContext outerTransaction) {
        var storage = this.attachedStorage();
        if (storage == null) return Integer.MAX_VALUE;

        try (var transaction = Transaction.openNested(outerTransaction)) {
            for (var entry : this.entries) {
                if (!entry.insert) continue;
                storage.insert(entry.variant(), entry.item.getCount(), transaction);
            }

            return (int) storage.insert(variant, Integer.MAX_VALUE, transaction);
        }
    }

    private boolean acceptsItem(ItemVariant variant) {
        if (this.filterStack.isEmpty()) return true;

        if (this.filterStack.getItem() == AffinityItems.PHANTOM_BUNDLE) {
            var contents = this.filterStack.getOrDefault(PhantomBundleItem.STACKS, PhantomBundleItem.StacksComponent.DEFAULT);
            if (!contents.stacks().isEmpty()) {
                return this.invertFilter != contents.stacks().stream().anyMatch(stack -> testFilter(stack, variant));
            }
        }

        return this.invertFilter != this.testFilter(this.filterStack, variant);
    }

    private boolean testFilter(ItemStack filter, ItemVariant variant) {
        var nameString = filter.getName().getString();
        var filterTag = nameString.startsWith("#") && Identifier.tryParse(nameString.substring(1)) instanceof Identifier tagId
            ? TagKey.of(RegistryKeys.ITEM, tagId)
            : null;

        if (filterTag != null) {
            if (!variant.getItem().getRegistryEntry().isIn(filterTag)) return false;
        } else {
            if (filter.getItem() != variant.getItem()) return false;
        }

        if (this.ignoreData) return true;

        if (this.ignoreDamage) {
            var standard = filter.getComponentChanges().withRemovedIf(type -> type == DataComponentTypes.DAMAGE);
            if (filterTag != null) {
                standard = standard.withRemovedIf(type -> type == DataComponentTypes.CUSTOM_NAME);
            }

            return standard.equals(variant.getComponents().withRemovedIf(type -> type == DataComponentTypes.DAMAGE));
        } else {
            return variant.componentsMatch(filter.getComponentChanges());
        }
    }

    private List<ItemTransferNodeBlockEntity> linkedNodes(Predicate<Mode> modePredicate) {
        var nodes = new ArrayList<ItemTransferNodeBlockEntity>();

        for (var link : this.links) {
            if (!(this.world.getBlockEntity(link) instanceof ItemTransferNodeBlockEntity node)) continue;
            if (modePredicate.test(node.mode)) {
                nodes.add(node);
            }
        }

        return nodes;
    }

    public Set<BlockPos> links() {
        return this.links;
    }

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        entries.add(Entry.text(Text.empty(), Text.literal(StringUtils.capitalize(this.mode.id))));
        entries.add(Entry.icon(Text.literal(String.valueOf(this.stackSize)), 8, 0));
    }

    public @NotNull ActionResult onScroll(PlayerEntity player, boolean direction) {
        int newStackSize = direction ? this.stackSize * 2 : this.stackSize / 2;
        this.stackSize = MathHelper.clamp(newStackSize, 1, 64);

        return this.stackSize == newStackSize ? ActionResult.SUCCESS : ActionResult.CONSUME;
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.isSneaking()) {
            if (world.isClient) return ActionResult.SUCCESS;
            player.openHandledScreen(new NamedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return ItemTransferNodeBlockEntity.this.getCachedState().getBlock().getName();
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new ItemTransferNodeScreenHandler(syncId, inv, ItemTransferNodeBlockEntity.this);
                }
            });
        } else {
            this.mode = this.mode.next();
            this.markDirty();
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        var ctx = SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) registries));

        nbt.put(ctx, LINKS_KEY, this.links);
        nbt.put(ctx, ENTRIES_KEY, this.entries);

        nbt.put(ctx, MODE_KEY, this.mode);
        nbt.put(ctx, STACK_SIZE_KEY, this.stackSize);
        nbt.put(ctx, FILTER_STACK_KEY, this.filterStack);

        nbt.put(ctx, IGNORE_DAMAGE_KEY, this.ignoreDamage);
        nbt.put(ctx, IGNORE_DATA_KEY, this.ignoreData);
        nbt.put(ctx, INVERT_FILTER_KEY, this.invertFilter);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        var ctx = SerializationContext.attributes(RegistriesAttribute.of((DynamicRegistryManager) registries));

        this.links = nbt.get(ctx, LINKS_KEY);
        this.entries = nbt.get(ctx, ENTRIES_KEY);

        this.entries.clear();
        this.entries.addAll(nbt.get(ctx, ENTRIES_KEY));

        this.mode = nbt.get(ctx, MODE_KEY);
        this.stackSize = nbt.get(ctx, STACK_SIZE_KEY);
        this.setFilterStack(nbt.get(ctx, FILTER_STACK_KEY));

        this.ignoreDamage = nbt.get(ctx, IGNORE_DAMAGE_KEY);
        this.ignoreData = nbt.get(ctx, IGNORE_DATA_KEY);
        this.invertFilter = nbt.get(ctx, INVERT_FILTER_KEY);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeFromCopiedStackNbt(NbtCompound nbt) {
        nbt.remove("Links");
    }

    @Override
    public @Nullable CuboidRenderer.Cuboid getActiveOutline() {
        return CuboidRenderer.Cuboid.symmetrical(15, 15, 15);
    }

    public ItemStack previewItem() {
        return this.entries.isEmpty() || this.entries.get(0).age < 0
            ? ItemStack.EMPTY
            : this.entries.get(0).item;
    }

    public List<ItemStack> displayItems() {
        var list = new ArrayList<ItemStack>(this.entries.size());
        for (var entry : this.entries) {
            if (!entry.insert) continue;
            list.add(entry.item);
        }

        return list;
    }

    public @NotNull ItemStack filterStack() {
        return this.filterStack;
    }

    public void setFilterStack(ItemStack filterStack) {
        this.filterStack = filterStack.copyWithCount(1);
        this.markDirty();
    }

    public Direction facing() {
        return this.facing;
    }

    private static Vec3d particleOrigin(ItemTransferNodeBlockEntity node) {
        return Vec3d.ofCenter(node.pos).add(node.facing.getOffsetX() * .1, node.facing.getOffsetY() * .1, node.facing.getOffsetZ() * .1);
    }

    public static final class ItemEntry {

        public static final Endec<ItemEntry> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.BLOCK_POS.fieldOf("OriginNode", entry -> entry.originNode),
            MinecraftEndecs.ITEM_STACK.fieldOf("Item", entry -> entry.item),
            Endec.INT.fieldOf("Age", entry -> entry.age),
            Endec.BOOLEAN.fieldOf("Insert", entry -> entry.insert),
            ItemEntry::new
        );

        private final BlockPos originNode;
        private final ItemStack item;
        private final boolean insert;

        private int age;
        private @Nullable ItemVariant variant = null;

        public ItemEntry(BlockPos originNode, ItemStack item, int age, boolean insert) {
            this.originNode = originNode;
            this.item = item;
            this.age = age;
            this.insert = insert;
        }

        public ItemVariant variant() {
            if (this.variant == null) this.variant = ItemVariant.of(this.item);
            return this.variant;
        }
    }

    public enum Mode {
        SENDING,
        IDLE;

        public static final Endec<Mode> ENDEC = Endec.STRING.xmap(id -> "sending".equals(id) ? SENDING : IDLE, mode -> mode.id);

        public final String id;

        Mode() {
            this.id = this.name().toLowerCase(Locale.ROOT);
        }

        public Mode next() {
            return this == SENDING ? IDLE : SENDING;
        }
    }
}
