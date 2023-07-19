package io.wispforest.affinity.mixin;

import io.wispforest.affinity.component.AffinityComponents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.EmptyChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WorldView.class)
public interface WorldViewMixin {
    @ModifyArg(method = "getLightLevel(Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldView;getLightLevel(Lnet/minecraft/util/math/BlockPos;I)I"))
    private int useChunkAmbientDarkness(BlockPos pos, int orig) {
        if (this instanceof World w) {
            var chunk = w.getChunk(pos);

            if (chunk instanceof EmptyChunk) return orig;

            var component = chunk.getComponent(AffinityComponents.LOCAL_WEATHER);
//
//            if (component.getAmbientDarkness() != orig) {
//                System.out.printf("%s != %d%n", component.getAmbientDarkness(), orig);
//            }

            return component.getAmbientDarkness();
        }

        return orig;
    }
}
