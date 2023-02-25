package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.AethumLink;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class CreativeAethumFluxCacheBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {

    public CreativeAethumFluxCacheBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.CREATIVE_AETHUM_FLUX_CACHE, pos, state);
        this.fluxStorage.setFluxCapacity(Long.MAX_VALUE);
        this.fluxStorage.setFlux(Long.MAX_VALUE);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void tickServer() {
        var pushTargets = this.getLinksByType(AethumLink.Type.PUSH);
        if (!pushTargets.isEmpty()) {
            try (var transaction = Transaction.openOuter()) {
                for (var pushTarget : pushTargets) {
                    var targetNode = Affinity.AETHUM_NODE.find(world, pushTarget, null);
                    if (targetNode == null) continue;

                    targetNode.insert(Long.MAX_VALUE, transaction);
                }

                transaction.commit();
            }
        }
    }

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        super.appendTooltipEntries(entries);

        entries.remove(0);
        entries.add(0, Entry.icon(Text.translatable("text.affinity.tooltip.creative_flux_cache_flux_level"), 0, 0));
    }

    @Override
    public AethumLink.Type specialLinkType() {
        return AethumLink.Type.PUSH;
    }
}
