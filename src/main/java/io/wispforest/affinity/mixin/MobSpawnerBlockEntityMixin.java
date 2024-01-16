package io.wispforest.affinity.mixin;

import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.mixin.access.MobSpawnerLogicAccessor;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.spawner.MobSpawnerLogic;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MobSpawnerBlockEntity.class)
public class MobSpawnerBlockEntityMixin implements InquirableOutlineProvider {

    @Shadow
    @Final
    private MobSpawnerLogic logic;

    @Override
    public @Nullable CuboidRenderer.Cuboid getActiveOutline() {
        var spawnRange = ((MobSpawnerLogicAccessor) this.logic).affinity$getSpawnRange();
        return CuboidRenderer.Cuboid.symmetrical(spawnRange, 1, spawnRange);
    }
}
