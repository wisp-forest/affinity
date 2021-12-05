package io.wispforest.affinity.blockentity;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.ops.WorldOps;
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

public abstract class AetherNetworkMemberBlockEntity extends BlockEntity implements AetherNetworkMember {
    protected final List<BlockPos> LINKED_MEMBERS = new ArrayList<>();

    public AetherNetworkMemberBlockEntity(BlockEntityType<? extends AetherNetworkMemberBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        LINKED_MEMBERS.clear();

        for (var member : nbt.getLongArray("LinkedMembers")) {
            LINKED_MEMBERS.add(BlockPos.fromLong(member));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        var members = new long[LINKED_MEMBERS.size()];

        for (int i = 0; i < LINKED_MEMBERS.size(); i++) {
            members[i] = LINKED_MEMBERS.get(i).asLong();
        }

        nbt.putLongArray("LinkedMembers", members);
    }

    public void onBroken() {
        for (var memberPos : this.LINKED_MEMBERS) {
            var member = Affinity.AETHER_MEMBER.find(world, memberPos, null);
            if (member == null) continue;

            member.onLinkTargetRemoved(this.pos);
        }
    }

    @Override
    public long flux() {
        return 0;
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
    public NbtCompound toInitialChunkDataNbt() {
        final var nbt = new NbtCompound();
        this.writeNbt(nbt);
        return nbt;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        WorldOps.updateIfOnServer(this.world, this.pos);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}
