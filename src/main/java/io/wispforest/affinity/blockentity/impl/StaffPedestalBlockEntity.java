package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.block.impl.StaffPedestalBlock;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.item.StaffItem;
import io.wispforest.affinity.misc.SingleStackStorageProvider;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StaffPedestalBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity, TickedBlockEntity, InquirableOutlineProvider {

    private static final KeyedEndec<ItemStack> ITEM_KEY = BuiltInEndecs.ITEM_STACK.keyed("Item", ItemStack.EMPTY);

    @NotNull private ItemStack item = ItemStack.EMPTY;
    private final SingleStackStorageProvider storageProvider = new SingleStackStorageProvider(() -> this.item, stack -> this.item = stack, this::markDirty)
            .capacity(1)
            .canInsert(variant -> variant.getItem() instanceof StaffItem staff && staff.canBePlacedOnPedestal());

    private int time = 0;

    public StaffPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.STAFF_PEDESTAL, pos, state);
        this.fluxStorage.setFluxCapacity(16000);
        this.fluxStorage.setMaxInsert(4000);
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        return InteractionUtil.handleSingleItemContainer(
                world, pos, player, hand,
                stack -> stack.getItem() instanceof StaffItem staff && staff.canBePlacedOnPedestal(),
                InteractionUtil.InvalidBehaviour.DROP,
                this.storageProvider.getter,
                this.storageProvider.setter,
                this::markDirty
        );
    }

    @Override
    public void tickServer() {
        if (this.shouldNotTick()) return;
        this.time++;

        if (this.item.isEmpty() || !(this.item.getItem() instanceof StaffItem staff)) return;
        staff.pedestalTickServer((ServerWorld) this.world, this.pos, this);
    }

    @Override
    public void tickClient() {
        if (this.shouldNotTick()) return;
        this.time++;

        if (this.item.isEmpty() || !(this.item.getItem() instanceof StaffItem staff)) return;
        staff.pedestalTickClient(this.world, this.pos, this);
    }

    protected boolean shouldNotTick() {
        return this.world.getReceivedRedstonePower(this.pos) > 0;
    }

    @Override
    public @Nullable CuboidRenderer.Cuboid getActiveOutline() {
        if (!(this.item.getItem() instanceof StaffItem staff)) return null;

        var aoe = staff.getAreaOfEffect(this.world, this.pos, this);
        if (aoe == null) return null;

        return CuboidRenderer.Cuboid.of(
                new BlockPos(aoe.minX(), aoe.minY(), aoe.minZ()),
                new BlockPos(aoe.maxX(), aoe.maxY(), aoe.maxZ()),
                Color.ofRgb(0x3E54AC), Color.ofRgb(0x3E54AC)
        );
    }

    public @NotNull ItemStack getItem() {
        return this.item;
    }

    public int time() {
        return this.time;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasFlux(long flux) {
        return this.fluxStorage.flux() >= flux;
    }

    public void consumeFlux(long flux) {
        this.updateFlux(this.flux() - flux);
    }

    public Direction facing() {
        return this.getCachedState().get(StaffPedestalBlock.FACING);
    }

    public int down() {
        return this.facing() == Direction.UP ? -1 : 1;
    }

    public int up() {
        return this.facing() == Direction.UP ? 1 : -1;
    }

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        super.appendTooltipEntries(entries);

        if (this.item.isEmpty() || !(this.item.getItem() instanceof StaffItem staff)) return;
        staff.appendTooltipEntries(this.world, this.pos, this, entries);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.item = nbt.get(ITEM_KEY);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put(ITEM_KEY, this.item);
    }

    @Override
    public void onBroken() {
        super.onBroken();

        var pos = Vec3d.ofCenter(this.pos);
        ItemScatterer.spawn(this.world, pos.getX(), pos.getY(), pos.getZ(), this.item);
    }

    static {
        //noinspection UnstableApiUsage
        ItemStorage.SIDED.registerForBlockEntity((pedestal, direction) -> pedestal.storageProvider, AffinityBlocks.Entities.STAFF_PEDESTAL);
    }
}
