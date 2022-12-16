package io.wispforest.affinity.blockentity.template;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.AethumLink;
import io.wispforest.affinity.aethumflux.net.AethumNetworkMember;
import io.wispforest.affinity.aethumflux.storage.AethumFluxStorage;
import io.wispforest.affinity.client.render.CrosshairStatProvider;
import io.wispforest.affinity.misc.PreMangroveBasketCallback;
import io.wispforest.affinity.misc.util.NbtUtil;
import io.wispforest.affinity.network.FluxSyncHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public abstract class AethumNetworkMemberBlockEntity extends SyncedBlockEntity implements AethumNetworkMember, AethumFluxStorage.CommitCallback, CrosshairStatProvider, PreMangroveBasketCallback {

    protected final Map<BlockPos, AethumLink.Type> links = new HashMap<>();
    protected final AethumFluxStorage fluxStorage = new AethumFluxStorage(this);

    public AethumNetworkMemberBlockEntity(BlockEntityType<? extends AethumNetworkMemberBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void onBroken() {
        for (var memberPos : this.links.keySet()) {
            var member = Affinity.AETHUM_MEMBER.find(world, memberPos, null);
            if (member == null) continue;

            member.onLinkTargetRemoved(this.pos);
        }

        this.links.clear();
    }

    @Override
    public boolean preMangroveBasket(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        this.onBroken();
        return true;
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
        NbtUtil.readLinks(nbt, "LinkedMembers", links);
        this.fluxStorage.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        NbtUtil.writeLinks(nbt, "LinkedMembers", links);
        this.fluxStorage.writeNbt(nbt);
    }

    protected void sendFluxUpdate() {
        if (world.isClient) return;
        FluxSyncHandler.queueUpdate(this);
        this.world.markDirty(this.pos);
    }

    @Environment(EnvType.CLIENT)
    public void readFluxUpdate(long flux) {
        this.fluxStorage.setFlux(flux);
    }

    // -------
    // Linking
    // -------

    protected Set<BlockPos> getLinksByType(AethumLink.Type type) {
        var result = new HashSet<BlockPos>();
        for (var entry : this.links.entrySet()) {
            if (entry.getValue() != type) continue;
            result.add(entry.getKey());
        }
        return result;
    }

    @Override
    public Set<BlockPos> linkedMembers() {
        return links.keySet();
    }

    @Override
    public boolean isLinked(BlockPos pos) {
        return this.links.containsKey(pos);
    }

    @Override
    public boolean addLinkParent(BlockPos pos, AethumLink.Type type) {
        if (isLinked(pos)) return false;

        this.links.put(pos.toImmutable(), type);
        this.markDirty(true);

        return true;
    }

    @Override
    public void onLinkTargetRemoved(BlockPos pos) {
        this.links.remove(pos);
        this.markDirty(true);
    }

    // ------------
    // Flux methods
    // ------------

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        entries.add(new Entry(Text.of(String.valueOf(this.displayFlux())), 0, 0));
    }

    public void updateFlux(long flux) {
        if (this.fluxStorage.setFlux(flux)) this.sendFluxUpdate();
    }

    public long displayFlux() {
        return this.flux();
    }

    @Override
    public long flux() {
        return this.fluxStorage.flux();
    }

    @Override
    public long fluxCapacity() {
        return this.fluxStorage.fluxCapacity();
    }

    @Override
    public long insert(long max, TransactionContext transaction) {
        return this.fluxStorage.insert(max, transaction);
    }

    @Override
    public boolean canInsert() {
        return this.fluxStorage.canInsert();
    }

    @Override
    public long maxInsert() {
        return this.fluxStorage.maxInsert();
    }

    @Override
    public long extract(long max, TransactionContext transaction) {
        return this.fluxStorage.extract(max, transaction);
    }

    @Override
    public long maxExtract() {
        return this.fluxStorage.maxExtract();
    }

    @Override
    public boolean canExtract() {
        return this.fluxStorage.canExtract();
    }

    @Override
    public void onTransactionCommitted() {
        this.sendFluxUpdate();
    }
}
