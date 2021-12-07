package io.wispforest.affinity.blockentity.template;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.util.aethumflux.AethumFluxStorage;
import io.wispforest.affinity.util.aethumflux.AethumNetworkMember;
import io.wispforest.owo.ops.WorldOps;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public abstract class AethumNetworkMemberBlockEntity extends SyncedBlockEntity implements AethumNetworkMember, AethumFluxStorage.CommitCallback {

    protected final List<BlockPos> LINKED_MEMBERS = new ArrayList<>();
    protected final AethumFluxStorage fluxStorage = new AethumFluxStorage(this);

    public AethumNetworkMemberBlockEntity(BlockEntityType<? extends AethumNetworkMemberBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        LINKED_MEMBERS.clear();

        for (var member : nbt.getLongArray("LinkedMembers")) {
            LINKED_MEMBERS.add(BlockPos.fromLong(member));
        }
        this.fluxStorage.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        var members = new long[LINKED_MEMBERS.size()];

        for (int i = 0; i < LINKED_MEMBERS.size(); i++) {
            members[i] = LINKED_MEMBERS.get(i).asLong();
        }

        nbt.putLongArray("LinkedMembers", members);
        this.fluxStorage.writeNbt(nbt);
    }

    public void onBroken() {
        for (var memberPos : this.LINKED_MEMBERS) {
            var member = Affinity.AETHUM_MEMBER.find(world, memberPos, null);
            if (member == null) continue;

            member.onLinkTargetRemoved(this.pos);
        }
    }

    @Override
    public List<BlockPos> linkedMembers() {
        return LINKED_MEMBERS;
    }

    @Override
    public boolean isLinked(BlockPos pos) {
        return LINKED_MEMBERS.contains(pos);
    }

    @Override
    public boolean addLinkParent(BlockPos pos) {
        if (isLinked(pos)) return false;

        this.LINKED_MEMBERS.add(pos);
        this.markDirty();

        return true;
    }

    @Override
    public void onLinkTargetRemoved(BlockPos pos) {
        this.LINKED_MEMBERS.remove(pos);
        this.markDirty();
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
        this.markDirty();
    }
}
