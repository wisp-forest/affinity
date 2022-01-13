package io.wispforest.affinity.blockentity.template;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.AethumLink;
import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.aethumflux.storage.AethumFluxStorage;
import io.wispforest.affinity.client.render.CrosshairStatProvider;
import io.wispforest.affinity.network.FluxSyncHandler;
import io.wispforest.affinity.util.NbtUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public abstract class AethumNetworkMemberBlockEntity extends SyncedBlockEntity implements AethumNetworkMember, AethumFluxStorage.CommitCallback, CrosshairStatProvider {

    protected final Map<BlockPos, AethumLink.Type> LINKS = new HashMap<>();
    protected final AethumFluxStorage fluxStorage = new AethumFluxStorage(this);

    public AethumNetworkMemberBlockEntity(BlockEntityType<? extends AethumNetworkMemberBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void onBroken() {
        for (var memberPos : this.LINKS.keySet()) {
            var member = Affinity.AETHUM_MEMBER.find(world, memberPos, null);
            if (member == null) continue;

            member.onLinkTargetRemoved(this.pos);
        }
    }

    @Override
    public boolean acceptsLinks() {
        return true;
    }

    @Override
    public AethumLink.Type specialLinkType() {
        return AethumLink.Type.NORMAL;
    }

    // -------------
    // Serialization
    // -------------

    @Override
    public void readNbt(NbtCompound nbt) {
        NbtUtil.readLinks(nbt, "LinkedMembers", LINKS);
        this.fluxStorage.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        NbtUtil.writeLinks(nbt, "LinkedMembers", LINKS);
        this.fluxStorage.writeNbt(nbt);
    }

    protected void sendFluxUpdate() {
        if (world.isClient) return;
        FluxSyncHandler.queueUpdate(this);
    }

    @Environment(EnvType.CLIENT)
    public void readFluxUpdate(long flux) {
        this.fluxStorage.setFlux(flux);
    }

    // -------
    // Linking
    // -------

    @Override
    public Set<BlockPos> linkedMembers() {
        return LINKS.keySet();
    }

    @Override
    public boolean isLinked(BlockPos pos) {
        return this.LINKS.containsKey(pos);
    }

    @Override
    public boolean addLinkParent(BlockPos pos, AethumLink.Type type) {
        if (isLinked(pos)) return false;

        this.LINKS.put(pos.toImmutable(), type);
        this.markDirty(true);

        return true;
    }

    @Override
    public void onLinkTargetRemoved(BlockPos pos) {
        this.LINKS.remove(pos);
        this.markDirty(true);
    }

    // ------------
    // Flux methods
    // ------------

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        entries.add(new Entry(Text.of("Flux: " + this.flux()), 0, 0));
    }

    public void updateFlux(long flux) {
        if (this.fluxStorage.setFlux(flux)) this.sendFluxUpdate();
    }

    @Override
    public long flux() {
        return fluxStorage.flux();
    }

    @Override
    public long fluxCapacity() {
        return fluxStorage.fluxCapacity();
    }

    @Override
    public long insert(long max, TransactionContext transaction) {
        return fluxStorage.insert(max, transaction);
    }

    @Override
    public boolean canInsert() {
        return fluxStorage.canInsert();
    }

    @Override
    public long maxInsert() {
        return fluxStorage.maxInsert();
    }

    @Override
    public long extract(long max, TransactionContext transaction) {
        return fluxStorage.extract(max, transaction);
    }

    @Override
    public long maxExtract() {
        return fluxStorage.maxExtract();
    }

    @Override
    public boolean canExtract() {
        return fluxStorage.canExtract();
    }

    @Override
    public void onTransactionCommitted() {
        this.sendFluxUpdate();
    }
}
