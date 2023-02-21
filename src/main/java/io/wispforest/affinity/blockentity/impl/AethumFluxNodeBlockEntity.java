package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.AethumLink;
import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.aethumflux.net.AethumNetworkNode;
import io.wispforest.affinity.block.template.AbstractAethumFluxNodeBlock;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.ShardBearingAethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.item.AttunedShardItem;
import io.wispforest.affinity.misc.util.ListUtil;
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
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class AethumFluxNodeBlockEntity extends ShardBearingAethumNetworkMemberBlockEntity implements AethumNetworkNode, TickedBlockEntity, InteractableBlockEntity {

    @Environment(EnvType.CLIENT) public float renderShardCount = 1;

    private long lastTick = 0;
    private Collection<AethumNetworkMember> cachedMembers = null;

    private final DefaultedList<ItemStack> outerShards = DefaultedList.ofSize(5, ItemStack.EMPTY);
    private int outerShardCount = 0;
    private boolean allLinksValid = false;

    private final float shardHeight;
    private final boolean isUpgradeable;

    public AethumFluxNodeBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AETHUM_FLUX_NODE, pos, state);

        if (state.getBlock() instanceof AbstractAethumFluxNodeBlock nodeBlock) {
            this.shardHeight = nodeBlock.shardHeight();
            this.isUpgradeable = nodeBlock.isUpgradeable();
        } else {
            this.shardHeight = .5f;
            this.isUpgradeable = false;
        }

        this.fluxStorage.setFluxCapacity(32000);
    }

    // ------------------
    // Ticking / Transfer
    // ------------------

    @Override
    public void tickServer() {
        if (!this.validForTransfer()) return;
        if (lastTick == world.getTime()) return;
        this.lastTick = world.getTime();

        final var network = this.visitNetwork();
        final var nodes = new ArrayList<AethumFluxNodeBlockEntity>();
        final var members = new ArrayList<TransferMember>();

        long networkFlux = 0;
        long networkCapacity = 0;

        for (var nodePos : network) {
            var node = Affinity.AETHUM_NODE.find(world, nodePos, null);
            if (!(node instanceof AethumFluxNodeBlockEntity nodeEntity)) continue;

            nodes.add(nodeEntity);
            networkFlux += node.flux();
            networkCapacity += node.fluxCapacity();

            for (var member : node.membersWithNormalLink()) {
                members.add(new TransferMember(node, member));
            }
        }

        if (networkFlux < 0) networkFlux = 0;
        if (networkFlux > networkCapacity) networkFlux = networkCapacity;

        networkFlux = transfer(members, networkFlux, networkCapacity, TransferFunction.EXTRACT_FROM_MEMBER);
        networkFlux = transfer(members, networkFlux, networkCapacity, TransferFunction.INSERT_INTO_MEMBER);

        var fluxPerNode = (long) Math.ceil(networkFlux / (double) nodes.size());

        for (var node : nodes) {
            node.updateFlux(Math.min(networkFlux, fluxPerNode));

            networkFlux = Math.max(0, networkFlux - fluxPerNode);
        }
    }

    private long transfer(List<TransferMember> members, long networkFlux, long networkCapacity, TransferFunction function) {
        Collections.shuffle(members);
        members.sort(function.comparator());

        try (var transaction = Transaction.openOuter()) {
            for (var transferMember : members) {
                networkFlux = function.transfer(transferMember, networkFlux, networkCapacity, transaction);
                if (networkFlux < 0 || networkFlux > networkCapacity) break;
            }

            transaction.commit();
        }

        return networkFlux;
    }

    private Collection<BlockPos> visitNetwork() {
        if (!validForTransfer()) return Collections.emptyList();

        var visitedNodes = new ArrayList<BlockPos>();
        visitedNodes.add(this.pos);

        var queue = new ArrayDeque<>(this.links.keySet());

        while (!queue.isEmpty()) {
            var memberPos = queue.poll();
            if (!(Affinity.AETHUM_NODE.find(this.world, memberPos, null) instanceof AethumFluxNodeBlockEntity node)) continue;
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

    public Collection<AethumNetworkMember> membersWithNormalLink() {
        if (this.cachedMembers != null) return this.cachedMembers;

        this.cachedMembers = new ArrayList<>(this.links.size());
        for (var memberLink : links.keySet()) {
            if (links.get(memberLink) != AethumLink.Type.NORMAL) continue;
            final var member = Affinity.AETHUM_MEMBER.find(world, memberLink, null);

            if (member == null) continue;
            if (member instanceof AethumNetworkNode) continue;

            this.cachedMembers.add(member);
        }

        return cachedMembers;
    }

    @Override
    public AethumLink.Result createGenericLink(BlockPos pos, AethumLink.Type type) {
        if (isLinked(pos)) return AethumLink.Result.ALREADY_LINKED;

        var member = Affinity.AETHUM_MEMBER.find(world, pos, null);
        if (member == null) return AethumLink.Result.NO_TARGET;

        if (this.links.size() >= this.maxConnections()) return AethumLink.Result.TOO_MANY_LINKS;
        if (!this.pos.isWithinDistance(pos, this.tier.maxDistance() + 1)) return AethumLink.Result.OUT_OF_RANGE;

        if (member instanceof AethumNetworkNode node) {
            if (node.isLinked(this.pos)) return AethumLink.Result.ALREADY_LINKED;
            node.addNodeLink(this.pos);
        } else {
            if (!member.acceptsLinks()) return AethumLink.Result.NO_TARGET;
            if (!member.addLinkParent(this.pos, type)) return AethumLink.Result.ALREADY_LINKED;
        }

        this.links.put(pos.toImmutable(), type);
        this.cachedMembers = null;
        this.markDirty(true);

        return AethumLink.Result.LINK_CREATED;
    }

    @Override
    public AethumLink.Result destroyLink(BlockPos pos) {
        if (!isLinked(pos)) return AethumLink.Result.NOT_LINKED;

        var member = Affinity.AETHUM_MEMBER.find(world, pos, null);
        if (member == null) return AethumLink.Result.NO_TARGET;

        if (!member.isLinked(this.pos)) return AethumLink.Result.NOT_LINKED;

        if (member instanceof AethumNetworkNode node) {
            node.removeNodeLink(this.pos);
        } else {
            member.onLinkTargetRemoved(this.pos);
        }

        this.links.remove(pos.toImmutable());
        this.cachedMembers = null;
        this.markDirty(true);

        return AethumLink.Result.LINK_DESTROYED;
    }

    @Override
    public void addNodeLink(BlockPos pos) {
        this.links.put(pos.toImmutable(), AethumLink.Type.NORMAL);
        this.markDirty(true);
    }

    @Override
    public void removeNodeLink(BlockPos pos) {
        this.links.remove(pos.toImmutable());
        this.markDirty(true);
    }

    @Override
    public void onLinkTargetRemoved(BlockPos pos) {
        super.onLinkTargetRemoved(pos);
        this.cachedMembers = null;
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

    // -------------
    // Serialization
    // -------------

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        NbtUtil.readItemStackList(nbt, "OuterShards", this.outerShards);

        updatePropertyCache();
        this.cachedMembers = null;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtUtil.writeItemStackList(nbt, "OuterShards", this.outerShards);
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

    public void onBreakStart(PlayerEntity player) {

        if ((player.isSneaking() || this.outerShardCount < 1) && !this.shard.isEmpty()) {
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), this.shard.copy());

            this.shard = ItemStack.EMPTY;
            this.tier = AttunedShardTiers.NONE;

            this.markDirty(false);
        } else if (this.outerShardCount > 0) {
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), ListUtil.getAndRemoveLast(this.outerShards));

            this.markDirty(false);
        }

        updatePropertyCache();
    }

    @Override
    public void onBroken() {
        super.onBroken();
        ItemScatterer.spawn(world, pos, this.outerShards);
    }

    private void updatePropertyCache() {
        this.outerShardCount = ListUtil.nonEmptyStacks(this.outerShards);
        this.updateTransferRateForTier();

        boolean validityAccumulator = true;
        for (var link : this.links.keySet()) {
            validityAccumulator &= this.pos.isWithinDistance(link, this.tier.maxDistance() + 1);
        }
        this.allLinksValid = validityAccumulator;
    }

    // -------
    // Getters
    // -------

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        super.appendTooltipEntries(entries);
        entries.add(Entry.icon(Text.of(this.tier.maxTransfer() + "/t"), 8, 0));
        entries.add(Entry.icon(Text.of("" + this.links.size()), 16, 0));
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

    // ------------------------
    // Transfer utility classes
    // ------------------------

    @SuppressWarnings("UnstableApiUsage")
    private static class TransferMember {

        private final AethumNetworkMember member;
        private final long potentialInsert;
        private final long potentialExtract;

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
            public long transfer(TransferMember transferMember, long networkFlux, long networkCapacity, TransactionContext transactionContext) {
                return networkFlux - transferMember.member.insert(Math.min(transferMember.potentialInsert, networkFlux),
                        transactionContext);
            }

            @Override
            public Comparator<TransferMember> comparator() {
                return INSERT_SORT;
            }
        };

        TransferFunction EXTRACT_FROM_MEMBER = new TransferFunction() {
            @Override
            public long transfer(TransferMember transferMember, long networkFlux, long networkCapacity, TransactionContext transactionContext) {
                return networkFlux + transferMember.member.extract(Math.min(transferMember.potentialExtract, networkCapacity - networkFlux),
                        transactionContext);
            }

            @Override
            public Comparator<TransferMember> comparator() {
                return EXTRACT_SORT;
            }
        };

        long transfer(TransferMember transferMember, long networkFlux, long networkCapacity, TransactionContext transactionContext);

        Comparator<TransferMember> comparator();
    }
}
