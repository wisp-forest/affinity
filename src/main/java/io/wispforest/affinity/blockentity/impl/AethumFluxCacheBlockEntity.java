package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.AethumLink;
import io.wispforest.affinity.block.impl.AethumFluxCacheBlock;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.registries.AffinityBlocks;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"UnstableApiUsage", "deprecation"})
public class AethumFluxCacheBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {

    private boolean isPrimaryStorage;
    @Nullable private AethumFluxCacheBlockEntity parent = null;
    @Nullable private List<AethumFluxCacheBlockEntity> childCache = null;

    public AethumFluxCacheBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AETHUM_FLUX_CACHE, pos, state);

        this.fluxStorage.setFluxCapacity(128000);
        this.fluxStorage.setMaxInsert(512);

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

//        this.world.getServer().getPlayerManager()
//                .broadcast(Text.of(this.pos.getY() + ": Cache updated -> is parent: " + isPrimaryStorage),  MessageType.CHAT, null);

        if (this.isPrimaryStorage) updateChildCache();
        if (this.parent != null) parent.updateChildCache();
    }

    private void updateChildCache() {
        this.childCache = new ArrayList<>();
        this.parent = null;

        for (var pos : BlockPos.iterate(this.pos.up(), this.pos.add(0, this.world.getHeight() - this.pos.getY(), 0))) {
            final var state = this.world.getBlockState(pos);
            if (!state.isOf(AffinityBlocks.AETHUM_FLUX_CACHE) || state.get(AethumFluxCacheBlock.PART).isBase) break;

            if (!(this.world.getBlockEntity(pos) instanceof AethumFluxCacheBlockEntity cacheEntity)) break;
            cacheEntity.parent = this;

            moveChildLinksOntoSelf(cacheEntity);

            this.childCache.add(cacheEntity);
        }

//        this.world.getServer().getPlayerManager()
//                .broadcast(Text.of(this.pos.getY() + ": Parent updated - " + childCache.size() + " children cached"),  MessageType.CHAT, null);
    }

    private void moveChildLinksOntoSelf(AethumFluxCacheBlockEntity child) {
        if (!child.LINKS.isEmpty()) {
            for (var link : child.linkedMembers()) {
                final var targetNode = Affinity.AETHUM_NODE.find(this.world, link, null);
                if (targetNode == null) continue;

                // TODO this doesn't actually get removed for some reason
                targetNode.onLinkTargetRemoved(link);
                targetNode.createGenericLink(this.pos, AethumLink.Type.NORMAL);
            }

            child.LINKS.clear();
            child.markDirty(true);
        }
    }

    private void moveSelfLinksOntoChild(AethumFluxCacheBlockEntity child) {
        if (!LINKS.isEmpty()) {
            for (var link : linkedMembers()) {
                final var targetNode = Affinity.AETHUM_NODE.find(this.world, link, null);
                if (targetNode == null) continue;

                targetNode.onLinkTargetRemoved(link);

                // TODO this cant work because the child doesn't consider itself a parent yet
                targetNode.createGenericLink(child.pos, AethumLink.Type.NORMAL);
            }
        }
    }

    @Override
    public void onBroken() {
        super.onBroken();
        if (this.world.getBlockEntity(this.pos.up()) instanceof AethumFluxCacheBlockEntity child) {
            moveSelfLinksOntoChild(child);
        }
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
        if (pushTargets.isEmpty()) return;

        long flux = this.fluxStorage.flux();
        final long maxTransferPerNode = (long) Math.ceil(flux / (double) pushTargets.size());

        try (var transaction = Transaction.openOuter()) {
            for (var pushTarget : pushTargets) {
                var targetNode = Affinity.AETHUM_NODE.find(world, pushTarget, null);
                if (targetNode == null) return;

                flux -= targetNode.insert(Math.min(flux, maxTransferPerNode), transaction);
            }

            transaction.commit();
        }

        this.fluxStorage.setFlux(flux);
        this.markDirty(false);
    }

    private Set<BlockPos> findPushTargets() {
        return LINKS.entrySet().stream().filter(entry -> entry.getValue() == AethumLink.Type.PUSH).map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    @Override
    public AethumLink.Type specialLinkType() {
        return AethumLink.Type.PUSH;
    }
}
