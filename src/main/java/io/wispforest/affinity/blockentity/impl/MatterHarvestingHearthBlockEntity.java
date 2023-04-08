package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.block.impl.MatterHarvestingHearthBlock;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InteractableBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.item.WispMatterItem;
import io.wispforest.affinity.misc.SingleElementDefaultedList;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.ops.ItemOps;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.util.ImplementedInventory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MatterHarvestingHearthBlockEntity extends AethumNetworkMemberBlockEntity implements InteractableBlockEntity, TickedBlockEntity, ImplementedInventory, SidedInventory {

    private static final Vec3d LINK_ATTACHMENT_POINT = new Vec3d(0, -.35f, 0);

    private static final int[] AVAILABLE_SLOTS = new int[]{0};
    private static final NbtKey<ItemStack> CURRENTLY_HARVESTING_KEY = new NbtKey<>("CurrentlyHarvesting", NbtKey.Type.ITEM_STACK);
    private static final NbtKey<Integer> HARVEST_TICKS_KEY = new NbtKey<>("HarvestTicks", NbtKey.Type.INT);

    @NotNull private ItemStack currentlyHarvesting = ItemStack.EMPTY;
    private final SingleElementDefaultedList<ItemStack> inventoryProvider = new SingleElementDefaultedList<>(
            ItemStack.EMPTY, () -> this.currentlyHarvesting, stack -> this.currentlyHarvesting = stack
    );

    private int time = 0;
    private int harvestTicks = 0;

    public MatterHarvestingHearthBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.MATTER_HARVESTING_HEARTH, pos, state);

        this.fluxStorage.setFluxCapacity(8000);
        this.fluxStorage.setMaxExtract(100);
    }

    @Override
    public void tickServer() {
        this.time++;
        if (!(this.currentlyHarvesting.getItem() instanceof WispMatterItem matter)) {
            this.enforceBlockState(false);
            return;
        }

        this.enforceBlockState(true);
        if (this.time % 20 == 0) {
            this.updateFlux(Math.min(this.flux() + matter.wispType().aethumFluxPerSecond(), this.fluxCapacity()));
        }

        if (++this.harvestTicks < 400) return;

        this.harvestTicks = 0;
        if (!ItemOps.emptyAwareDecrement(this.currentlyHarvesting)) {
            this.currentlyHarvesting = ItemStack.EMPTY;
            this.markDirty();
        }
    }

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        super.appendTooltipEntries(entries);

        if (!(this.currentlyHarvesting.getItem() instanceof WispMatterItem matter)) return;
        entries.add(Entry.icon(Text.of(matter.wispType().aethumFluxPerSecond() + "/s"), 8, 0));
    }

    private void enforceBlockState(boolean lit) {
        if (this.getCachedState().get(MatterHarvestingHearthBlock.LIT) == lit) return;
        this.world.setBlockState(this.getPos(), this.getCachedState().with(MatterHarvestingHearthBlock.LIT, lit));
    }

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!this.currentlyHarvesting.isEmpty()) return ActionResult.PASS;

        var playerStack = player.getStackInHand(hand);
        if (!(playerStack.getItem() instanceof WispMatterItem)) return ActionResult.PASS;

        this.currentlyHarvesting = playerStack.copyWithCount(1);
        ItemOps.decrementPlayerHandItem(player, hand);

        return ActionResult.SUCCESS;
    }

    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(Random random) {
        if (this.currentlyHarvesting.isEmpty() || !(this.currentlyHarvesting.getItem() instanceof WispMatterItem matter)) {
            return;
        }

        ClientParticles.setParticleCount(5);
        ClientParticles.spawn(new DustParticleEffect(MathUtil.rgbToVec3f(matter.wispType().color()), 1), this.world, Vec3d.ofCenter(this.pos, .35), .25);

        if (random.nextFloat() > .5) return;
        ClientParticles.randomizeVelocity(.15);
        ClientParticles.spawn(ParticleTypes.END_ROD, this.world, Vec3d.ofCenter(this.pos, .35), .25);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.currentlyHarvesting = nbt.get(CURRENTLY_HARVESTING_KEY);
        this.harvestTicks = nbt.get(HARVEST_TICKS_KEY);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.put(CURRENTLY_HARVESTING_KEY, this.currentlyHarvesting);
        nbt.put(HARVEST_TICKS_KEY, this.harvestTicks);
    }

    @Override
    public Vec3d linkAttachmentPointOffset() {
        return LINK_ATTACHMENT_POINT;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventoryProvider;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return AVAILABLE_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return this.currentlyHarvesting.isEmpty();
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }
}
