package io.wispforest.affinity.mixin.access;

import net.minecraft.world.biome.OverworldBiomeCreator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(OverworldBiomeCreator.class)
public interface OverworldBiomeCreatorInvoker {

    @Invoker("getSkyColor")
    static int affinity$getSkyColor(float temperature) {
        throw new AssertionError("how");
    }

}
