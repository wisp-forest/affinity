package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.registries.AffinityBlocks;
import io.wispforest.affinity.util.aethumflux.AethumLink;
import io.wispforest.affinity.util.aethumflux.AethumNetworkMember;
import io.wispforest.affinity.util.aethumflux.AethumNetworkNode;
import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class AethumFluxNodeBlockEntity extends AethumNetworkMemberBlockEntity implements AethumNetworkNode, TickedBlockEntity {

    private Collection<AethumNetworkMember> cachedMembers = null;
    private long lastTick = 0;

    public AethumFluxNodeBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AETHUM_FLUX_NODE, pos, state);

        this.fluxStorage.setFluxCapacity(32000);
        this.fluxStorage.setMaxInsert(256);
        this.fluxStorage.setMaxExtract(256);
    }

    public void onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) return;

        final var network = visitNetwork();
        player.sendMessage(Text.of("Network size: " + network.size()), false);

        ClientParticles.persist();
        ClientParticles.setParticleCount(20);

        for (var nodePos : network) {
            ClientParticles.spawnLine(ParticleTypes.GLOW, world, Vec3d.ofCenter(nodePos), Vec3d.ofCenter(nodePos.add(0, 2, 0)), 0);
        }

        ClientParticles.reset();
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

//        System.out.printf("[#%d] Ticking network of %d nodes\n", lastTick, nodes.size());
//        System.out.printf("[#%d] Flux Before: %d | Capacity: %d\n", lastTick, networkFlux, networkCapacity);

        networkFlux = transfer(members, networkFlux, networkCapacity, TransferFunction.EXTRACT_FROM_MEMBER);
        networkFlux = transfer(members, networkFlux, networkCapacity, TransferFunction.INSERT_INTO_MEMBER);

        var fluxPerNode = (long) Math.ceil(networkFlux / (double) nodes.size());

        for (var node : nodes) {
            node.fluxStorage.setFlux(Math.min(networkFlux, fluxPerNode));
            node.markDirty(false);

            networkFlux -= fluxPerNode;
        }

//        System.out.printf("[#%d] Flux After: %d | Capacity: %d\n", lastTick, networkFlux, networkCapacity);
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
        this.cachedMembers = null;
    }

    @Override
    public void addNodeLink(BlockPos pos) {
        this.LINKED_MEMBERS.add(pos);
        this.markDirty(true);
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
