package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.AethumLink;
import io.wispforest.affinity.aethumflux.shards.AttunedShardTiers;
import io.wispforest.affinity.block.impl.AethumFluxCacheBlock;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.ShardBearingAethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.registries.AffinityBlocks;
import io.wispforest.owo.network.annotations.ElementType;
import io.wispforest.owo.ops.ItemOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"UnstableApiUsage", "deprecation"})
public class AethumFluxCacheBlockEntity extends ShardBearingAethumNetworkMemberBlockEntity implements TickedBlockEntity {

    @Environment(EnvType.CLIENT) public float renderFluxY = 0;

    private boolean isPrimaryStorage;
    @Nullable private ParentStorageReference parentRef = null;
    @Nullable private List<AethumFluxCacheBlockEntity> childCache = null;

    public AethumFluxCacheBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AETHUM_FLUX_CACHE, pos, state);

        this.fluxStorage.setFluxCapacity(128000);

        this.isPrimaryStorage = state.get(AethumFluxCacheBlock.PART).isBase;
    }

    @Override
    public void setCachedState(BlockState state) {
        super.setCachedState(state);
        updateForStateChange(state);
    }

    public void updateForStateChange(BlockState state) {
        if (this.world == null) return;
        if (this.world.isClient) return;

        this.childCache = null;
        final var part = state.get(AethumFluxCacheBlock.PART);

        if (this.isPrimaryStorage && !part.isBase && this.world.getBlockEntity(this.pos.add(0, -1, 0)) instanceof AethumFluxCacheBlockEntity cacheEntity) {
            cacheEntity.updateForStateChange(cacheEntity.getCachedState());
            this.isPrimaryStorage = false;
            return;
        }

        this.isPrimaryStorage = part.isBase;

        if (this.isPrimaryStorage) updateChildCache();
        if (this.parentRef != null) parentRef.entity.updateChildCache();
    }

    private void updateChildCache() {
        this.childCache = new ArrayList<>();

        for (var pos : BlockPos.iterate(this.pos.up(), this.pos.add(0, this.world.getHeight() - this.pos.getY(), 0))) {
            final var state = this.world.getBlockState(pos);
            if (!state.isOf(AffinityBlocks.AETHUM_FLUX_CACHE) || state.get(AethumFluxCacheBlock.PART).isBase) break;

            if (!(this.world.getBlockEntity(pos) instanceof AethumFluxCacheBlockEntity cacheEntity)) break;
            cacheEntity.parentRef = new ParentStorageReference(this, this.childCache.size());
            cacheEntity.isPrimaryStorage = false;

            cacheEntity.tier = this.tier;
            cacheEntity.updateTransferRateForTier();

            moveChildLinksOntoSelf(cacheEntity);

            this.childCache.add(cacheEntity);
        }

        if (!this.childCache.isEmpty()) {
            AffinityNetwork.CHANNEL.serverHandle(PlayerLookup.tracking(this))
                    .send(new CacheChildrenPacket(this.pos, this.childCache.stream().map(BlockEntity::getPos).toList()));
        }
    }

    private void moveChildLinksOntoSelf(AethumFluxCacheBlockEntity child) {
        if (!child.links.isEmpty()) {
            for (var link : child.linkedMembers()) {
                final var targetNode = Affinity.AETHUM_NODE.find(this.world, link, null);
                if (targetNode == null) continue;

                targetNode.onLinkTargetRemoved(child.pos);
                targetNode.createGenericLink(this.pos, child.links.get(link));
            }

            child.links.clear();
            child.markDirty(true);
        }
    }

    private void moveSelfLinksOntoChild(AethumFluxCacheBlockEntity child) {
        child.isPrimaryStorage = true;

        if (!links.isEmpty()) {
            for (var link : linkedMembers()) {
                final var targetNode = Affinity.AETHUM_NODE.find(this.world, link, null);
                if (targetNode == null) continue;

                targetNode.onLinkTargetRemoved(link);
                targetNode.createGenericLink(child.pos, links.get(link));
            }
        }

        child.isPrimaryStorage = false;
    }

    @Override
    public void onBroken() {
        super.onBroken();
        if (!(this.world.getBlockEntity(this.pos.up()) instanceof AethumFluxCacheBlockEntity child)) return;
        moveSelfLinksOntoChild(child);
    }

    @Override
    public boolean acceptsLinks() {
        return this.isPrimaryStorage;
    }

    @Override
    public void tickServer() {
        if (!isPrimaryStorage) return;
        if (this.childCache == null) this.updateChildCache();

        var pushTargets = findPushTargets();

        long totalFlux = this.fluxStorage.flux();
        if (!this.childCache.isEmpty()) totalFlux += this.childCache.stream().mapToLong(value -> value.fluxStorage.flux()).sum();

        if (!pushTargets.isEmpty() && this.tier.maxTransfer() > 0) {
            final long maxTransferPerNode = Math.min(this.tier.maxTransfer(), (long) Math.ceil(totalFlux / (double) pushTargets.size()));

            try (var transaction = Transaction.openOuter()) {
                for (var pushTarget : pushTargets) {
                    var targetNode = Affinity.AETHUM_NODE.find(world, pushTarget, null);
                    if (targetNode == null) return;

                    totalFlux -= targetNode.insert(Math.min(totalFlux, maxTransferPerNode), transaction);
                }

                transaction.commit();
            }
        }

        final long perCacheCap = this.fluxStorage.fluxCapacity();

        long insertedFlux = Math.min(perCacheCap, totalFlux);
        this.updateFlux(insertedFlux);
        totalFlux -= insertedFlux;

        if (!this.childCache.isEmpty()) {
            for (var child : childCache) {
                insertedFlux = Math.min(perCacheCap, totalFlux);

                child.updateFlux(insertedFlux);

                totalFlux -= insertedFlux;
            }
        }
    }

    @Override
    public long insert(long max, TransactionContext transaction) {
        if (this.childCache != null && this.fluxStorage.flux() >= this.fluxStorage.fluxCapacity()) {
            for (var child : childCache) {
                if (child.fluxStorage.flux() >= child.fluxStorage.fluxCapacity()) continue;
                return child.insert(max, transaction);
            }
        }

        return super.insert(max, transaction);
    }

    private Set<BlockPos> findPushTargets() {
        return links.entrySet().stream().filter(entry -> entry.getValue() == AethumLink.Type.PUSH).map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    @Override
    public long visualFlux() {
        if (this.parentRef == null) return super.visualFlux();
        final var parent = parentRef.entity;

        if (parent.childCache == null || parent.childCache.isEmpty()) return super.visualFlux();
        return parent.flux() + parent.childCache.stream().mapToLong(AethumNetworkMemberBlockEntity::flux).sum();
    }

    @Environment(EnvType.CLIENT)
    public void readChildren(List<BlockPos> children) {
        if (this.childCache == null) this.childCache = new ArrayList<>();
        this.childCache.clear();
        this.parentRef = new ParentStorageReference(this, -1);

        for (var childPos : children) {
            if (!(world.getBlockEntity(childPos) instanceof AethumFluxCacheBlockEntity child)) return;
            child.parentRef = new ParentStorageReference(this, this.childCache.size());
            child.isPrimaryStorage = false;

            child.tier = this.tier;
            child.updateTransferRateForTier();

            this.childCache.add(child);
        }
    }

    @Override
    protected void updateTransferRateForTier() {
        this.fluxStorage.setMaxInsert(this.tier.maxTransfer());
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!this.isPrimaryStorage) return this.parentRef == null ? ActionResult.PASS : this.parentRef.entity.onUse(player, hand, hit);

        final var playerStack = player.getStackInHand(hand);

        if (playerStack.isEmpty()) {
            if (this.shard.isEmpty()) return ActionResult.PASS;
            player.setStackInHand(hand, this.shard.copy());

            this.shard = ItemStack.EMPTY;
            this.markDirty();
        } else {
            if (this.shard.isEmpty() && !AttunedShardTiers.forItem(playerStack.getItem()).isNone()) {
                this.shard = ItemOps.singleCopy(playerStack);
                ItemOps.decrementPlayerHandItem(player, hand);

                this.markDirty();
            } else if (ItemOps.canStack(playerStack, this.shard)) {
                this.shard = ItemStack.EMPTY;

                playerStack.increment(1);
                player.setStackInHand(hand, playerStack);

                this.markDirty();
            } else {
                return ActionResult.PASS;
            }
        }

        this.tier = AttunedShardTiers.forItem(this.shard.getItem());
        this.updateTransferRateForTier();
        if (!world.isClient) this.updateChildCache();

        return ActionResult.SUCCESS;
    }

    @Override
    public AethumLink.Type specialLinkType() {
        return AethumLink.Type.PUSH;
    }

    public ParentStorageReference parent() {
        return parentRef;
    }

    static {
        AffinityNetwork.CHANNEL.registerClientbound(CacheChildrenPacket.class, (message, access) -> {
            if (!(access.runtime().world.getBlockEntity(message.cachePos()) instanceof AethumFluxCacheBlockEntity cache)) return;
            cache.readChildren(message.children());
        });
    }

    public static record CacheChildrenPacket(BlockPos cachePos, @ElementType(BlockPos.class) List<BlockPos> children) {}

    public static record ParentStorageReference(AethumFluxCacheBlockEntity entity, int index) {

        public boolean previousIsNotFull() {
            final var previous = previous();
            return previous != null && previous.flux() < previous.fluxCapacity();
        }

        public boolean nextIsEmpty() {
            final var next = next();
            return next == null || next.flux() == 0;
        }

        @SuppressWarnings("ConstantConditions")
        public @Nullable AethumFluxCacheBlockEntity previous() {
            if (index == 0) return entity;
            return validIndex(index - 1) ? entity.childCache.get(index - 1) : null;
        }

        @SuppressWarnings("ConstantConditions")
        public @Nullable AethumFluxCacheBlockEntity next() {
            return validIndex(index + 1) ? entity.childCache.get(index + 1) : null;
        }

        private boolean validIndex(int index) {
            return entity.childCache != null && index >= 0 && index < entity.childCache.size();
        }

    }
}
