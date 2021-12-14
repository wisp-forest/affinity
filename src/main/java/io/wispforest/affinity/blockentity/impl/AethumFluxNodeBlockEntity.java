package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.AethumLink;
import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.aethumflux.net.AethumNetworkNode;
import io.wispforest.affinity.aethumflux.shards.AttunedShardTier;
import io.wispforest.affinity.aethumflux.shards.AttunedShardTiers;
import io.wispforest.affinity.block.template.AbstractAethumFluxNodeBlock;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.item.AttunedShardItem;
import io.wispforest.affinity.item.IridescenceWandItem;
import io.wispforest.affinity.registries.AffinityBlocks;
import io.wispforest.affinity.util.ListUtil;
import io.wispforest.affinity.util.NbtUtil;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.particles.ClientParticles;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class AethumFluxNodeBlockEntity extends AethumNetworkMemberBlockEntity implements AethumNetworkNode, TickedBlockEntity {

    private long lastTick = 0;
    private Collection<AethumNetworkMember> cachedMembers = null;

    @NotNull private ItemStack shard = ItemStack.EMPTY;
    @NotNull private AttunedShardTier tier = AttunedShardTiers.EMPTY;

    private final DefaultedList<ItemStack> outerShards = DefaultedList.ofSize(5, ItemStack.EMPTY);
    private int outerShardCount = 0;

    private final float shardHeight;
    private final boolean supportsOuterShards;

    public AethumFluxNodeBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AETHUM_FLUX_NODE, pos, state);

        if (state.getBlock() instanceof AbstractAethumFluxNodeBlock nodeBlock) {
            this.shardHeight = nodeBlock.shardHeight();
            this.supportsOuterShards = nodeBlock.supportsOuterShards();
        } else {
            this.shardHeight = .5f;
            this.supportsOuterShards = false;
        }

        this.fluxStorage.setFluxCapacity(32000);
    }

    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        var playerStack = player.getStackInHand(hand);

        final var playerItem = playerStack.getItem();
        if (!(playerItem instanceof IridescenceWandItem)) {
            // TODO make this not suck

            if (playerStack.isEmpty()) {

                if (!this.supportsOuterShards || this.outerShards.isEmpty() || player.isSneaking()) {
                    player.setStackInHand(hand, this.shard.copy());
                    this.shard = ItemStack.EMPTY;
                    this.tier = AttunedShardTiers.EMPTY;

                } else if (!this.outerShards.isEmpty()) {
                    player.setStackInHand(hand, ListUtil.getAndRemoveLast(this.outerShards));
                    this.outerShardCount = ListUtil.nonEmptyStacks(this.outerShards);
                }

            } else if (playerItem instanceof AttunedShardItem shardItem) {

//                if (ItemOps.canStack(playerStack, this.shard)) {
//                    playerStack.increment(1);
//                    this.shard = ItemStack.EMPTY;
//                    this.tier = AttunedShardTiers.EMPTY;
//
//                    return ActionResult.SUCCESS;
//                } else if (ItemOps.canStack(playerStack, ListUtil.peekLast(this.outerShards))) {
//                    playerStack.increment(1);
//
//                    ListUtil.getAndRemoveLast(this.outerShards);
//                    this.outerShardCount = ListUtil.nonEmptyStacks(this.outerShards);
//
//                    return ActionResult.SUCCESS;
//                }

                if (this.shard.isEmpty()) {
                    ItemOps.decrementPlayerHandItem(player, hand);
                    this.shard = ItemOps.singleCopy(playerStack);
                    this.tier = shardItem.tier();
                } else if (playerStack.isOf(Items.AMETHYST_SHARD) && this.canAcceptShards()) {
                    ItemOps.decrementPlayerHandItem(player, hand);

                    ListUtil.addItem(this.outerShards, ItemOps.singleCopy(playerStack));
                    this.outerShardCount = ListUtil.nonEmptyStacks(this.outerShards);
                }

            }

        } else {
            if (!world.isClient) return ActionResult.PASS;

            final var network = visitNetwork();
            player.sendMessage(Text.of("Network size: " + network.size()), false);

            ClientParticles.persist();
            ClientParticles.setParticleCount(20);

            for (var nodePos : network) {
                ClientParticles.spawnLine(ParticleTypes.GLOW, world, Vec3d.ofCenter(nodePos), Vec3d.ofCenter(nodePos.add(0, 2, 0)), 0);
            }

            ClientParticles.reset();
        }

        return ActionResult.SUCCESS;
    }

    private boolean canAcceptShards() {
        return this.supportsOuterShards && this.outerShards.stream().anyMatch(ItemStack::isEmpty);
    }

    public void onBroken() {
        ItemScatterer.spawn(world, pos, this.outerShards);
        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), this.shard);
    }

    @Override
    public void tickServer() {
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

            for (var member : node.getLinkedMembers()) {
                members.add(new TransferMember(node, member));
            }
        }

        if (networkFlux < 0) networkFlux = 0;
        if (networkFlux > networkCapacity) networkFlux = networkCapacity;

        networkFlux = transfer(members, networkFlux, networkCapacity, TransferFunction.EXTRACT_FROM_MEMBER);
        networkFlux = transfer(members, networkFlux, networkCapacity, TransferFunction.INSERT_INTO_MEMBER);

        var fluxPerNode = (long) Math.ceil(networkFlux / (double) nodes.size());

        for (var node : nodes) {
            node.fluxStorage.setFlux(Math.min(networkFlux, fluxPerNode));
            node.markDirty(false);

            networkFlux -= fluxPerNode;
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
        var visitedNodes = new ArrayList<BlockPos>();
        visitedNodes.add(this.pos);

        var queue = new ArrayDeque<>(LINKED_MEMBERS);

        while (!queue.isEmpty()) {
            var nodePos = queue.poll();
            if (!(Affinity.AETHUM_NODE.find(world, nodePos, null) instanceof AethumFluxNodeBlockEntity node)) continue;

            visitedNodes.add(nodePos);
            node.lastTick = world.getTime();

            for (var neighbor : node.LINKED_MEMBERS) {
                if (visitedNodes.contains(neighbor) || queue.contains(neighbor)) continue;
                queue.add(neighbor);
            }
        }

        return visitedNodes;
    }

    public Collection<AethumNetworkMember> getLinkedMembers() {
        if (this.cachedMembers != null) return this.cachedMembers;

        this.cachedMembers = new ArrayList<>(this.LINKED_MEMBERS.size());
        for (var memberPos : LINKED_MEMBERS) {
            final var member = Affinity.AETHUM_MEMBER.find(world, memberPos, null);

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

        if (member instanceof AethumNetworkNode node) {
            if (node.isLinked(this.pos)) return AethumLink.Result.ALREADY_LINKED;
            node.addNodeLink(this.pos);
        } else {
            if (!member.addLinkParent(this.pos, type)) return AethumLink.Result.ALREADY_LINKED;
        }

        this.LINKED_MEMBERS.add(pos);
        this.cachedMembers = null;
        this.markDirty(true);

        return AethumLink.Result.SUCCESS;
    }

    @Override
    public void onLinkTargetRemoved(BlockPos pos) {
        super.onLinkTargetRemoved(pos);
        this.cachedMembers = null;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.shard = ItemStack.fromNbt(nbt.getCompound("Shard"));
        NbtUtil.readItemStackList(nbt, "OuterShards", this.outerShards);

        this.fluxStorage.setMaxExtract(this.tier.maxTransfer());
        this.fluxStorage.setMaxInsert(this.tier.maxTransfer());

        this.cachedMembers = null;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.put("Shard", this.shard.writeNbt(new NbtCompound()));
        NbtUtil.writeItemStackList(nbt, "OuterShards", this.outerShards);
    }

    @Override
    public void addNodeLink(BlockPos pos) {
        this.LINKED_MEMBERS.add(pos);
        this.markDirty(true);
    }

    public AttunedShardTier tier() {
        return tier;
    }

    public int outerShardCount() {
        return this.outerShardCount;
    }

    public boolean supportsOuterShards() {
        return this.supportsOuterShards;
    }

    public float shardHeight() {
        return this.shardHeight;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static class TransferMember {

        private final AethumNetworkMember member;
        private final long potentialInsert;
        private final long potentialExtract;

        public TransferMember(AethumNetworkNode node, AethumNetworkMember member) {
            this.member = member;

            try (var transaction = Transaction.openOuter()) {
                this.potentialExtract = member.extract(node.maxInsert(), transaction);
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
