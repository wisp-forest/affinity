package io.wispforest.affinity.mixin;

import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.client.render.CuboidRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin extends BlockEntity implements InquirableOutlineProvider {

    @Shadow
    int level;

    public BeaconBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public @Nullable CuboidRenderer.Cuboid getActiveOutline() {
        if (this.level == 0) return null;

        int size = this.level * 10 + 10;
        return CuboidRenderer.Cuboid.of(new BlockPos(-size, -size, -size), new BlockPos(size + 1, size + this.world.getHeight() + 1, size + 1));
    }
}
