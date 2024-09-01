package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.AethumLink;
import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.aethumflux.net.AethumNetworkNode;
import io.wispforest.affinity.block.template.AbstractAethumFluxNodeBlock;
import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.ShardBearingAethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.item.AttunedShardItem;
import io.wispforest.affinity.misc.util.ListUtil;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.misc.util.NbtUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.attunedshards.AttunedShardTiers;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class AethumFluxNodeBlockEntity extends ShardBearingAethumNetworkMemberBlockEntity implements AethumNetworkNode, TickedBlockEntity, InteractableBlockEntity, InquirableOutlineProvider {

    @Environment(EnvType.CLIENT) public float renderShardCount;
    @Environment(EnvType.CLIENT) public float shardActivity;
    @Environment(EnvType.CLIENT) public double time;

    private long lastTick = 0;
    private final Map<AethumLink.Type, Collection<AethumNetworkMember>> cachedMembers = new HashMap<>();

    private final DefaultedList<ItemStack> outerShards = DefaultedList.ofSize(Affinity.config().maxFluxNodeShards(), ItemStack.EMPTY);
    private int outerShardCount = 0;
    private boolean allLinksValid = false;

    private final float shardHeight;
    private final Vec3d linkAttachmentPoint;
    private final boolean isUpgradeable;

    public AethumFluxNodeBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AETHUM_FLUX_NODE, pos, state);

        if (state.getBlock() instanceof AbstractAethumFluxNodeBlock nodeBlock) {
            this.shardHeight = nodeBlock.shardHeight();
            this.isUpgradeable = nodeBlock.isUpgradeable();
            this.linkAttachmentPoint = nodeBlock.linkAttachmentPoint();
        } else {
            this.shardHeight = .5f;
            this.isUpgradeable = false;
            this.linkAttachmentPoint = Vec3d.ZERO;
        }

        this.fluxStorage.setFluxCapacity(16000);

        if (Affinity.onClient()) {
            this.renderShardCount = 1f;
            this.shardActivity = 1f;
            this.time = ThreadLocalRandom.current().nextLong(0, 2000);
        }
    }

    // ------------------
    // Ticking / Transfer
    // ------------------

    @Override
    @Environment(EnvType.CLIENT)
    public void tickClient() {
        if (this.links.isEmpty() || !this.validForTransfer()) return;
        if (this.world.random.nextFloat() >= .5f) return;

        var linkIter = this.links.keySet().iterator();
        int linkIndex = this.world.random.nextInt(this.links.size());

        for (int i = 0; i < linkIndex; i++) linkIter.next();

        var otherPos = linkIter.next();
        var otherMember = Affinity.AETHUM_MEMBER.find(this.world, otherPos, null);
        if (otherMember == null) return;

        var thisPoint = Vec3d.ofCenter(this.pos).add(this.linkAttachmentPoint);
        var otherPoint = Vec3d.ofCenter(otherPos).add(otherMember.linkAttachmentPointOffset());

        var offset = otherPoint.subtract(thisPoint).multiply(.01f + this.world.random.nextFloat() * .99f);
        var startPos = thisPoint.add(offset);
        var endPos = thisPoint.add(offset.normalize().multiply(.25f + this.world.random.nextFloat() * .5f));

        ClientParticles.setParticleCount(1 + this.world.random.nextInt(MathHelper.ceil(offset.length())));
        ClientParticles.spawnLine(new DustParticleEffect(MathUtil.rgbToVec3f(Affinity.AETHUM_FLUX_COLOR.rgb()), .5f), this.world, startPos, endPos, .015f);
    }

    @Override
    public void tickServer() {
        if (!this.validForTransfer()) return;
        if (this.lastTick == world.getTime()) return;
        this.lastTick = world.getTime();

        final var network = this.visitNetwork();
        final var nodes = new ArrayList<AethumFluxNodeBlockEntity>();

        final var standardMembers = new ArrayList<TransferMember>();
        int standardInsertMembers = 0;

        final var priorityMembers = new ArrayList<TransferMember>();
        int priorityInsertMembers = 0;

        long networkFlux = 0;
        long networkCapacity = 0;

        for (var nodePos : network) {
            var node = Affinity.AETHUM_NODE.find(world, nodePos, null);
            if (!(node instanceof AethumFluxNodeBlockEntity nodeEntity)) continue;

            nodes.add(nodeEntity);
            networkFlux += node.flux();
            networkCapacity += node.fluxCapacity();

            for (var member : node.membersByLinkType(AethumLink.Type.NORMAL)) {
                var transferMember = new TransferMember(node, member);
                standardMembers.add(transferMember);

                if (transferMember.potentialInsert > 0) standardInsertMembers++;
            }

            for (var member : node.membersByLinkType(AethumLink.Type.PRIORITIZED)) {
                var transferMember = new TransferMember(node, member);
                priorityMembers.add(transferMember);

                if (transferMember.potentialInsert > 0) priorityInsertMembers++;
            }
        }

        if (networkFlux < 0) networkFlux = 0;
        if (networkFlux > networkCapacity) networkFlux = networkCapacity;

        networkFlux = this.transfer(priorityMembers, networkFlux, networkCapacity, Long.MAX_VALUE, TransferFunction.EXTRACT_FROM_MEMBER);
        networkFlux = this.transfer(standardMembers, networkFlux, networkCapacity, Long.MAX_VALUE, TransferFunction.EXTRACT_FROM_MEMBER);

        if (networkFlux > 0 && priorityInsertMembers > 0) {
            networkFlux = this.transfer(priorityMembers, networkFlux, networkCapacity, networkFlux / priorityInsertMembers, TransferFunction.INSERT_INTO_MEMBER);
            networkFlux = this.transfer(priorityMembers, networkFlux, networkCapacity, Long.MAX_VALUE, TransferFunction.INSERT_INTO_MEMBER);
        }

        if (networkFlux > 0 && standardInsertMembers > 0) {
            networkFlux = this.transfer(standardMembers, networkFlux, networkCapacity, networkFlux / standardInsertMembers, TransferFunction.INSERT_INTO_MEMBER);
            networkFlux = this.transfer(standardMembers, networkFlux, networkCapacity, Long.MAX_VALUE, TransferFunction.INSERT_INTO_MEMBER);
        }

        var fluxPerNode = (long) Math.ceil(networkFlux / (double) nodes.size());

        for (var node : nodes) {
            node.updateFlux(Math.min(networkFlux, fluxPerNode));

            networkFlux = Math.max(0, networkFlux - fluxPerNode);
        }
    }

    private long transfer(List<TransferMember> members, long networkFlux, long networkCapacity, long maxTransfer, TransferFunction function) {
        Collections.shuffle(members);
        members.sort(function.comparator());

        try (var transaction = Transaction.openOuter()) {
            for (var transferMember : members) {
                networkFlux = function.transfer(transferMember, networkFlux, networkCapacity, maxTransfer, transaction);
                if (networkFlux < 0 || networkFlux > networkCapacity) break;
            }

            transaction.commit();
        }

        return networkFlux;
    }

    private Collection<BlockPos> visitNetwork() {
        if (!this.validForTransfer()) return Collections.emptyList();

        var visitedNodes = new ArrayList<BlockPos>();
        visitedNodes.add(this.pos);

        var queue = new ArrayDeque<>(this.links.keySet());

        while (!queue.isEmpty()) {
            var memberPos = queue.poll();
            if (!(Affinity.AETHUM_NODE.find(this.world, memberPos, null) instanceof AethumFluxNodeBlockEntity node)) {
                continue;
            }
            if (!node.validForTransfer()) continue;

            visitedNodes.add(memberPos);
            node.lastTick = this.world.getTime();

            for (var neighbor : node.linkedMembers()) {
                if (visitedNodes.contains(neighbor) || queue.contains(neighbor)) continue;
                queue.add(neighbor);
            }
        }

        return visitedNodes;
    }

    // -------
    // Linking
    // -------

    @Override
    public @Nullable CuboidRenderer.Cuboid getActiveOutline() {
        int distance = this.tier.maxDistance();
        return CuboidRenderer.Cuboid.symmetrical(distance, distance, distance);
    }

    public Collection<AethumNetworkMember> membersByLinkType(AethumLink.Type type) {
        if (this.cachedMembers.containsKey(type)) return this.cachedMembers.get(type);

        var cachedMembers = new ArrayList<AethumNetworkMember>(this.links.size());
        for (var memberLink : links.keySet()) {
            if (links.get(memberLink) != type) continue;
            final var member = Affinity.AETHUM_MEMBER.find(world, memberLink, null);

            if (member == null) continue;
            if (member instanceof AethumNetworkNode) continue;

            cachedMembers.add(member);
        }

        this.cachedMembers.put(type, cachedMembers);
        return cachedMembers;
    }

    @Override
    public LinkResult createGenericLink(BlockPos pos, AethumLink.Type type) {
        if (isLinked(pos)) return LinkResult.ALREADY_LINKED;

        var member = Affinity.AETHUM_MEMBER.find(world, pos, null);
        if (member == null) return LinkResult.NO_TARGET;

        if (this.links.size() >= this.maxConnections()) return LinkResult.TOO_MANY_LINKS;
        if (!this.isInRange(pos)) return LinkResult.OUT_OF_RANGE;

        if (member instanceof AethumNetworkNode node) {
            if (node.isLinked(this.pos)) return LinkResult.ALREADY_LINKED;

            var result = node.addNodeLink(this.pos);
            if (result != LinkResult.LINK_CREATED) return result;
        } else {
            if (!member.acceptsLinks()) return LinkResult.NO_TARGET;
            if (!member.addLinkParent(this.pos, type)) return LinkResult.ALREADY_LINKED;
        }

        this.links.put(pos.toImmutable(), type);
        this.cachedMembers.clear();
        this.markDirty(true);

        return LinkResult.LINK_CREATED;
    }

    @Override
    public LinkResult destroyLink(BlockPos pos) {
        if (!isLinked(pos)) return LinkResult.NOT_LINKED;

        var member = Affinity.AETHUM_MEMBER.find(world, pos, null);
        if (member == null) return LinkResult.NO_TARGET;

        if (!member.isLinked(this.pos)) return LinkResult.NOT_LINKED;

        if (member instanceof AethumNetworkNode node) {
            node.removeNodeLink(this.pos);
        } else {
            member.onLinkTargetRemoved(this.pos);
        }

        this.links.remove(pos.toImmutable());
        this.cachedMembers.clear();
        this.markDirty(true);

        return LinkResult.LINK_DESTROYED;
    }

    @Override
    public LinkResult addNodeLink(BlockPos pos) {
        if (!this.isInRange(pos)) return LinkResult.OUT_OF_RANGE;

        this.links.put(pos.toImmutable(), AethumLink.Type.NORMAL);
        this.markDirty(true);

        return LinkResult.LINK_CREATED;
    }

    protected boolean isInRange(BlockPos pos) {
        return Math.abs(this.pos.getX() - pos.getX()) <= this.tier.maxDistance()
                && Math.abs(this.pos.getY() - pos.getY()) <= this.tier.maxDistance()
                && Math.abs(this.pos.getZ() - pos.getZ()) <= this.tier.maxDistance();
    }

    @Override
    public void removeNodeLink(BlockPos pos) {
        this.links.remove(pos.toImmutable());
        this.markDirty(true);
    }

    @Override
    public void onLinkTargetRemoved(BlockPos pos) {
        super.onLinkTargetRemoved(pos);
        this.cachedMembers.clear();
    }

    @Override
    public boolean acceptsLinks() {
        return this.links.size() < this.maxConnections();
    }

    public int maxConnections() {
        return 5 + this.outerShardCount * 2;
    }

    public boolean validForTransfer() {
        if (this.world.getReceivedRedstonePower(this.pos) > 0) return false;
        return this.hasShard() && this.allLinksValid && this.links.size() <= this.maxConnections();
    }

    @Override
    public long insert(long max, TransactionContext transaction) {
        if (!this.validForTransfer()) return 0;
        return super.insert(max, transaction);
    }

    @Override
    public long extract(long max, TransactionContext transaction) {
        if (!this.validForTransfer()) return 0;
        return super.extract(max, transaction);
    }

    // -------------
    // Serialization
    // -------------

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        NbtUtil.readItemStackList(nbt, "OuterShards", this.outerShards, registries);

        updatePropertyCache();
        this.cachedMembers.clear();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        NbtUtil.writeItemStackList(nbt, "OuterShards", this.outerShards, registries);
    }

    // -----------
    // Interaction
    // -----------

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        var playerStack = player.getStackInHand(hand);

        if (!playerStack.isEmpty()) {

            if (playerStack.isOf(Items.AMETHYST_SHARD)) {
                if (this.shard.isEmpty()) {
                    this.shard = ItemOps.singleCopy(playerStack);
                    this.tier = AttunedShardTiers.CRUDE;

                    ItemOps.decrementPlayerHandItem(player, hand);

                    updatePropertyCache();
                    this.markDirty(false);

                    return ActionResult.SUCCESS;
                } else if (this.isUpgradeable && this.outerShardCount < this.outerShards.size()) {
                    ListUtil.addItem(this.outerShards, ItemOps.singleCopy(playerStack));

                    ItemOps.decrementPlayerHandItem(player, hand);

                    updatePropertyCache();
                    this.markDirty(false);

                    return ActionResult.SUCCESS;
                }
            } else if (this.isUpgradeable() && this.shard.isEmpty() && playerStack.getItem() instanceof AttunedShardItem shardItem) {
                this.shard = ItemOps.singleCopy(playerStack);
                this.tier = shardItem.tier();

                ItemOps.decrementPlayerHandItem(player, hand);

                updatePropertyCache();
                this.markDirty(false);

                return ActionResult.SUCCESS;
            }

        }

        return ActionResult.PASS;
    }

    public ActionResult onAttack(PlayerEntity player) {
        if (player.isSneaking() && this.shard.isEmpty()) return ActionResult.PASS;

        if ((player.isSneaking() || this.outerShardCount < 1) && !this.shard.isEmpty()) {
            ItemScatterer.spawn(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.shard.copy());

            this.shard = ItemStack.EMPTY;
            this.tier = AttunedShardTiers.NONE;

            this.markDirty(false);
            this.updatePropertyCache();

            return ActionResult.SUCCESS;
        } else if (this.outerShardCount > 0) {
            ItemScatterer.spawn(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), ListUtil.getAndRemoveLast(this.outerShards));

            this.markDirty(false);
            this.updatePropertyCache();

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void onBroken() {
        super.onBroken();
        ItemScatterer.spawn(world, pos, this.outerShards);
    }

    private void updatePropertyCache() {
        this.outerShardCount = ListUtil.nonEmptyStacks(this.outerShards);
        this.updateTransferRateForTier();

        this.allLinksValid = true;
        for (var link : this.links.keySet()) {
            this.allLinksValid &= this.isInRange(link);
        }
    }

    // -------
    // Getters
    // -------

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        super.appendTooltipEntries(entries);
        entries.add(Entry.icon(Text.of(this.tier.maxTransfer() * 20 + "/s"), 8, 0));
        entries.add(Entry.icon(Text.of(String.valueOf(this.links.size())), 16, 0));
    }

    public int outerShardCount() {
        return this.outerShardCount;
    }

    public boolean isUpgradeable() {
        return this.isUpgradeable;
    }

    public float shardHeight() {
        return this.shardHeight;
    }

    @Override
    public Vec3d linkAttachmentPointOffset() {
        return this.linkAttachmentPoint;
    }

    @Override
    public AethumLink.Type specialLinkType() {
        return AethumLink.Type.PRIORITIZED;
    }

    // ------------------------
    // Transfer utility classes
    // ------------------------

    private static class TransferMember {

        private final AethumNetworkMember member;
        private long potentialInsert;
        private long potentialExtract;

        public TransferMember(AethumNetworkNode node, AethumNetworkMember member) {
            this.member = member;

            try (var transaction = Transaction.openOuter()) {
                this.potentialExtract = member.extract(node.maxInsert(), transaction);
            }

            try (var transaction = Transaction.openOuter()) {
                this.potentialInsert = member.insert(node.maxExtract(), transaction);
            }
        }
    }

    private interface TransferFunction {

        Comparator<TransferMember> INSERT_SORT = Comparator.comparingLong(value -> value.potentialInsert);
        Comparator<TransferMember> EXTRACT_SORT = Comparator.comparingLong(value -> value.potentialExtract);

        TransferFunction INSERT_INTO_MEMBER = new TransferFunction() {
            @Override
            public long transfer(TransferMember transferMember, long networkFlux, long networkCapacity, long maxTransfer, TransactionContext transactionContext) {
                long inserted = transferMember.member.insert(
                        Math.min(maxTransfer, Math.min(transferMember.potentialInsert, networkFlux)),
                        transactionContext
                );

                transferMember.potentialInsert -= inserted;
                return networkFlux - inserted;
            }

            @Override
            public Comparator<TransferMember> comparator() {
                return INSERT_SORT;
            }
        };

        TransferFunction EXTRACT_FROM_MEMBER = new TransferFunction() {
            @Override
            public long transfer(TransferMember transferMember, long networkFlux, long networkCapacity, long maxTransfer, TransactionContext transactionContext) {
                long extracted = transferMember.member.extract(
                        Math.min(maxTransfer, Math.min(transferMember.potentialExtract, networkCapacity - networkFlux)),
                        transactionContext
                );

                transferMember.potentialExtract -= extracted;
                return networkFlux + extracted;
            }

            @Override
            public Comparator<TransferMember> comparator() {
                return EXTRACT_SORT;
            }
        };

        long transfer(TransferMember transferMember, long networkFlux, long networkCapacity, long maxTransfer, TransactionContext transactionContext);

        Comparator<TransferMember> comparator();
    }
}
