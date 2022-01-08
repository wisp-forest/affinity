package io.wispforest.affinity.blockentity.template;

import io.wispforest.affinity.aethumflux.shards.AttunedShardTier;
import io.wispforest.affinity.aethumflux.shards.AttunedShardTiers;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public abstract class ShardBearingAethumNetworkMemberBlockEntity extends AethumNetworkMemberBlockEntity {

    @NotNull protected ItemStack shard = ItemStack.EMPTY;
    @NotNull protected AttunedShardTier tier = AttunedShardTiers.NONE;

    public ShardBearingAethumNetworkMemberBlockEntity(BlockEntityType<? extends AethumNetworkMemberBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit);

    public boolean hasShard() {
        return this.tier != AttunedShardTiers.NONE;
    }

    public AttunedShardTier tier() {
        return tier;
    }

    protected void updateTransferRateForTier() {
        this.fluxStorage.setMaxExtract(this.tier.maxTransfer());
        this.fluxStorage.setMaxInsert(this.tier.maxTransfer());
    }

    @Override
    public void onBroken() {
        super.onBroken();
        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), this.shard);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("Shard", this.shard.writeNbt(new NbtCompound()));
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.shard = ItemStack.fromNbt(nbt.getCompound("Shard"));
        this.tier = AttunedShardTiers.forItem(this.shard.getItem());
        this.updateTransferRateForTier();
    }
}
