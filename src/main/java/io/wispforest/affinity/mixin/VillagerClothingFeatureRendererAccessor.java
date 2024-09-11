package io.wispforest.affinity.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.render.entity.feature.VillagerClothingFeatureRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerClothingFeatureRenderer.class)
public interface VillagerClothingFeatureRendererAccessor {
    @Accessor("LEVEL_TO_ID")
    static Int2ObjectMap<Identifier> affinity$LevelToIdMap() {throw new UnsupportedOperationException();}
}
