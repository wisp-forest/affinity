package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.block.impl.ArcaneTreetapBlock;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityParticleSystems;
import io.wispforest.affinity.object.AffinityPoiTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;

import java.util.HashSet;
import java.util.Set;

public class AssemblyAugmentBlockEntity extends BlockEntity implements TickedBlockEntity {

    private final Set<BlockPos> treetapCache = new HashSet<>();
    private final SimpleInventory craftingInput = new SimpleInventory(9);

    public AssemblyAugmentBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ASSEMBLY_AUGMENT, pos, state);
        this.craftingInput.addListener(sender -> this.markDirty());
    }

    @Override
    public void tickServer() {
        if (this.world.getTime() % 20 != 0) return;

        this.treetapCache.clear();
        ((ServerWorld) this.world).getPointOfInterestStorage()
                .getInCircle(type -> type.value() == AffinityPoiTypes.ARCANE_TREETAP, this.pos, 10, PointOfInterestStorage.OccupationStatus.ANY)
                .map(PointOfInterest::getPos)
                .filter(poi -> ArcaneTreetapBlock.isProperlyAttached(this.world, poi))
                .forEach(this.treetapCache::add);

        AffinityParticleSystems.AFFINE_CANDLE_BREWING.spawn(this.world, Vec3d.ofCenter(this.pos),
                new AffinityParticleSystems.CandleData(this.treetapCache.stream().map(Vec3d::ofCenter).toList())
        );
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.craftingInput.clear();
        Inventories.readNbt(nbt, this.craftingInput.stacks);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        Inventories.writeNbt(nbt, this.craftingInput.stacks);
    }

    @Override
    public void markDirty() {
        if (this.world == null) return;
        this.world.markDirty(this.pos);
    }

    public SimpleInventory craftingInput() {
        return this.craftingInput;
    }

    public Set<BlockPos> treetapCache() {
        return this.treetapCache;
    }
}
