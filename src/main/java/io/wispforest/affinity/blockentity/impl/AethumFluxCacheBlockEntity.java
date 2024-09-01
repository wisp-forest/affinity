package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.AethumLink;
import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.aethumflux.net.MultiblockAethumNetworkMember;
import io.wispforest.affinity.block.impl.AethumFluxCacheBlock;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.ShardBearingAethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.attunedshards.AttunedShardTier;
import io.wispforest.affinity.object.attunedshards.AttunedShardTiers;
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
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"deprecation"})
public class AethumFluxCacheBlockEntity extends ShardBearingAethumNetworkMemberBlockEntity implements TickedBlockEntity, InteractableBlockEntity, MultiblockAethumNetworkMember {

    private static final Vec3d LINK_ATTACHMENT_POINT = new Vec3d(0, -.45, 0);

    @Environment(EnvType.CLIENT) public float renderFluxY;
    @Environment(EnvType.CLIENT) public boolean tickedOnce;

    private boolean isPrimaryStorage;
    private ParentStorageReference parentRef = null;
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
        this.tier = AttunedShardTier.forItem(this.shard.getItem());

        if (this.isPrimaryStorage) this.updateChildCache();
        if (this.parentRef != null && parentRef.entity != this) parentRef.entity.updateChildCache();
    }

    private void updateChildCache() {
        this.childCache = new ArrayList<>();
        this.parentRef = new ParentStorageReference(this, -1);

        if (this.world.getBlockEntity(this.pos.up()) instanceof AethumFluxCacheBlockEntity cache && !cache.shard.isEmpty() && this.shard.isEmpty()) {
            this.shard = cache.shard;
            this.tier = cache.tier;
            this.updateTransferRateForTier();

            cache.shard = ItemStack.EMPTY;
            cache.tier = AttunedShardTiers.NONE;
        }

        for (var pos : BlockPos.iterate(this.pos.up(), this.pos.add(0, this.world.getHeight() - this.pos.getY(), 0))) {
            final var state = this.world.getBlockState(pos);
            if (!state.isOf(AffinityBlocks.AETHUM_FLUX_CACHE) || state.get(AethumFluxCacheBlock.PART).isBase) break;

            if (!(this.world.getBlockEntity(pos) instanceof AethumFluxCacheBlockEntity cacheEntity)) break;
            cacheEntity.parentRef = new ParentStorageReference(this, this.childCache.size());
            cacheEntity.isPrimaryStorage = false;

            cacheEntity.tier = this.tier;
            cacheEntity.updateTransferRateForTier();

            this.moveChildLinksOntoSelf(cacheEntity);

            this.childCache.add(cacheEntity);
        }

        AffinityNetwork.CHANNEL.serverHandle(PlayerLookup.tracking(this)).send(new CacheDataUpdatePacket(this));
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

    private void tryMoveSelfLinksOntoChild() {
        if (!(this.world.getBlockEntity(this.pos.up()) instanceof AethumFluxCacheBlockEntity child)) return;
        child.isPrimaryStorage = true;

        if (!this.links.isEmpty()) {
            for (var link : this.linkedMembers()) {
                final var targetNode = Affinity.AETHUM_NODE.find(this.world, link, null);
                if (targetNode == null) continue;

                targetNode.onLinkTargetRemoved(this.pos);
                targetNode.createGenericLink(child.pos, this.links.get(link));
            }
        }

        child.isPrimaryStorage = false;
        this.links.clear();
    }

    @Override
    public void onBroken() {
        this.tryMoveSelfLinksOntoChild();
        super.onBroken();
    }

    @Override
    public boolean beforeMangroveBasketCapture(World world, BlockPos pos, MutableObject<BlockState> state, BlockEntity blockEntity) {
        this.tryMoveSelfLinksOntoChild();
        super.beforeMangroveBasketCapture(world, pos, state, blockEntity);

        if (state.getValue().get(AethumFluxCacheBlock.PART) != AethumFluxCacheBlock.Part.STANDALONE) {
            state.setValue(state.getValue().with(AethumFluxCacheBlock.PART, AethumFluxCacheBlock.Part.STANDALONE));
        }

        return true;
    }

    @Override
    public boolean acceptsLinks() {
        return this.isPrimaryStorage;
    }

    @Override
    public void tickClient() {
        if (this.tickedOnce) return;

        if (this.isPrimaryStorage) {
            AffinityNetwork.CHANNEL.clientHandle().send(new RequestCacheDataPacket(this.pos));
        }

        this.tickedOnce = true;
    }

    @Override
    public void tickServer() {
        if (!isPrimaryStorage) return;
        if (this.childCache == null) this.updateChildCache();

        var pushTargets = this.getLinksByType(AethumLink.Type.PUSH);

        long totalFlux = this.fluxStorage.flux();
        if (!this.childCache.isEmpty()) {
            totalFlux += this.childCache.stream().mapToLong(value -> value.fluxStorage.flux()).sum();
        }

        if (!pushTargets.isEmpty() && this.tier.maxTransfer() > 0) {
            final long maxTransferPerNode = Math.min(this.tier.maxTransfer(), (long) Math.ceil(totalFlux / (double) pushTargets.size()));

            try (var transaction = Transaction.openOuter()) {
                for (var pushTarget : pushTargets) {
                    var targetNode = Affinity.AETHUM_NODE.find(world, pushTarget, null);
                    if (targetNode == null) continue;

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
            long totalAvailable = max;

            for (var child : childCache) {
                if (child.fluxStorage.flux() >= child.fluxStorage.fluxCapacity()) continue;
                max -= child.directInsert(max, transaction);
                if (max <= 0) break;
            }

            return totalAvailable - max;
        }

        return this.directInsert(max, transaction);
    }

    private long directInsert(long max, TransactionContext transaction) {
        return super.insert(max, transaction);
    }

    @Override
    public void updateFlux(long flux) {
        var fluxBefore = this.flux();
        super.updateFlux(flux);

        if (fluxBefore != flux) {
            this.world.updateComparators(this.pos, this.getCachedState().getBlock());
        }
    }

    @Override
    public void onTransactionCommitted() {
        super.onTransactionCommitted();
        this.world.updateComparators(this.pos, this.getCachedState().getBlock());
    }

    @Override
    public Collection<BlockPos> memberBlocks() {
        if (this.parentRef == null || this.parentRef.entity.childCache == null) return List.of();

        var members = this.parentRef.entity.childCache.stream().map(BlockEntity::getPos).collect(Collectors.toList());
        members.add(this.parentRef.entity.pos);
        return members;
    }

    @Override
    public boolean isParent() {
        return this.parentRef != null && this.parentRef.entity == this;
    }

    @Override
    public long displayFlux() {
        if (this.parentRef == null) return super.displayFlux();
        final var parent = parentRef.entity;

        if (parent.childCache == null || parent.childCache.isEmpty()) return super.displayFlux();
        return parent.flux() + parent.childCache.stream().mapToLong(AethumNetworkMemberBlockEntity::flux).sum();
    }

    @Override
    public long displayFluxCapacity() {
        if (this.parentRef == null) return super.displayFluxCapacity();
        final var parent = parentRef.entity;

        if (parent.childCache == null || parent.childCache.isEmpty()) return super.displayFluxCapacity();
        return parent.fluxCapacity() + parent.childCache.stream().mapToLong(AethumNetworkMemberBlockEntity::fluxCapacity).sum();
    }

    @Environment(EnvType.CLIENT)
    public void updateForStateChangeClient(List<BlockPos> children, AttunedShardTier tier) {
        if (this.childCache == null) this.childCache = new ArrayList<>();
        this.childCache.clear();
        this.parentRef = new ParentStorageReference(this, -1);
        this.isPrimaryStorage = true;
        this.tier = tier;

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
    protected void setTierFromNbt(AttunedShardTier tier) {
        if (!this.isPrimaryStorage) return;
        super.setTierFromNbt(tier);
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!this.isPrimaryStorage) {
            return this.parentRef == null ? ActionResult.PASS : this.parentRef.entity.onUse(player, hand, hit);
        }

        final var playerStack = player.getStackInHand(hand);

        if (playerStack.isOf(Items.GLASS_BOTTLE) && this.flux() >= 1000) {
            this.updateFlux(this.flux() - 1000);

            ItemOps.decrementPlayerHandItem(player, hand);
            player.getInventory().offerOrDrop(AffinityItems.AETHUM_FLUX_BOTTLE.getDefaultStack());
            return ActionResult.SUCCESS;
        }

        if (playerStack.isEmpty()) {
            if (this.shard.isEmpty()) return ActionResult.PASS;
            player.setStackInHand(hand, this.shard.copy());

            this.shard = ItemStack.EMPTY;
            this.markDirty();
        } else {
            if (this.shard.isEmpty() && !AttunedShardTier.forItem(playerStack.getItem()).isNone()) {
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

        this.tier = AttunedShardTier.forItem(this.shard.getItem());
        this.updateTransferRateForTier();
        if (!world.isClient) this.updateChildCache();

        return ActionResult.SUCCESS;
    }

    @Override
    public AethumLink.Type specialLinkType() {
        return AethumLink.Type.PUSH;
    }

    @Override
    public AethumNetworkMember parent() {
        return this.parentRef.entity;
    }

    public ParentStorageReference parentRef() {
        return this.parentRef;
    }

    @Override
    public Vec3d linkAttachmentPointOffset() {
        return LINK_ATTACHMENT_POINT;
    }

    static {
        AffinityNetwork.CHANNEL.registerServerbound(RequestCacheDataPacket.class, (message, access) -> {
            if (!(access.player().getWorld().getBlockEntity(message.pos()) instanceof AethumFluxCacheBlockEntity cache)) {
                return;
            }
            AffinityNetwork.CHANNEL.serverHandle(access.player()).send(new CacheDataUpdatePacket(cache));
        });

        AffinityNetwork.CHANNEL.registerClientbound(CacheDataUpdatePacket.class, (message, access) -> {
            if (!(access.runtime().world.getBlockEntity(message.cachePos()) instanceof AethumFluxCacheBlockEntity cache)) {
                return;
            }
            cache.updateForStateChangeClient(message.children(), AttunedShardTier.forItem(message.shard.getItem()));
        });
    }

    public record RequestCacheDataPacket(BlockPos pos) {}

    public record CacheDataUpdatePacket(BlockPos cachePos, List<BlockPos> children, ItemStack shard) {
        public CacheDataUpdatePacket(AethumFluxCacheBlockEntity cache) {
            this(cache.pos, cache.childCache == null ? Collections.emptyList() : cache.childCache.stream().map(BlockEntity::getPos).toList(), cache.shard);
        }
    }

    public record ParentStorageReference(AethumFluxCacheBlockEntity entity, int index) {

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
