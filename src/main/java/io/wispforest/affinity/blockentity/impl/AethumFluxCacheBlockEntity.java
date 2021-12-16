package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.AethumLink;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.registries.AffinityBlocks;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class AethumFluxCacheBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {

    public AethumFluxCacheBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AETHUM_FLUX_CACHE, pos, state);

        this.fluxStorage.setFluxCapacity(128000);
        this.fluxStorage.setMaxInsert(512);
    }

    @Override
    public void tickServer() {
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
