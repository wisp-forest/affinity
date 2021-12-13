package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.registries.AffinityBlocks;
import io.wispforest.affinity.util.NbtUtil;
import io.wispforest.affinity.util.aethumflux.AethumLink;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class AethumFluxCacheBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {

    private final List<BlockPos> PUSH_LINKS = new ArrayList<>();

    public AethumFluxCacheBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AETHUM_FLUX_CACHE, pos, state);

        this.fluxStorage.setFluxCapacity(128000);
        this.fluxStorage.setMaxInsert(512);
    }

    @Override
    public void tickServer() {
        if (this.PUSH_LINKS.isEmpty()) return;

        long flux = this.fluxStorage.flux();
        final long maxTransferPerNode = (long) Math.ceil(flux / (double) this.PUSH_LINKS.size());

        try (var transaction = Transaction.openOuter()) {
            for (var pushTarget : PUSH_LINKS) {
                var targetNode = Affinity.AETHUM_NODE.find(world, pushTarget, null);
                if (targetNode == null) return;

                flux -= targetNode.insert(Math.min(flux, maxTransferPerNode), transaction);
            }

            transaction.commit();
        }

        this.fluxStorage.setFlux(flux);
        this.markDirty(false);
    }

    @Override
    public boolean addLinkParent(BlockPos pos, AethumLink.Type type) {
        if (!super.addLinkParent(pos, type)) return false;
        if (type != AethumLink.Type.PUSH) return true;

        this.PUSH_LINKS.add(pos);
        return true;
    }

    @Override
    public AethumLink.Type specialLinkType() {
        return AethumLink.Type.PUSH;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        NbtUtil.readBlockPosList(nbt, "PushLinks", PUSH_LINKS);
        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        NbtUtil.writeBlockPosList(nbt, "PushLinks", PUSH_LINKS);
        super.writeNbt(nbt);
    }
}
