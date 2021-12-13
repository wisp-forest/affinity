package io.wispforest.affinity.blockentity.template;

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

public abstract class SyncedBlockEntity extends BlockEntity {

    public SyncedBlockEntity(BlockEntityType<? extends SyncedBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void markDirty() {
        this.markDirty(true);
    }

    public void markDirty(boolean updateComparators) {
        if (this.world == null) return;

        this.world.markDirty(this.pos);
        if (updateComparators) this.world.updateComparators(this.pos, this.getCachedState().getBlock());

        WorldOps.updateIfOnServer(this.world, this.pos);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        final var nbt = new NbtCompound();
        this.writeNbt(nbt);
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}
