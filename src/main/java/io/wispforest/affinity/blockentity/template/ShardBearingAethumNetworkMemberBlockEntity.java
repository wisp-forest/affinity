package io.wispforest.affinity.blockentity.template;

import io.wispforest.affinity.object.attunedshards.AttunedShardTier;
import io.wispforest.affinity.object.attunedshards.AttunedShardTiers;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
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
        return !this.tier.isNone();
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
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.put("Shard", this.shard.encodeAllowEmpty(registries));
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        this.shard = ItemStack.fromNbtOrEmpty(registries, nbt.getCompound("Shard"));
        this.setTierFromNbt(AttunedShardTier.forItem(this.shard.getItem()));
        this.updateTransferRateForTier();
    }

    protected void setTierFromNbt(AttunedShardTier tier) {
        this.tier = tier;
    }
}
