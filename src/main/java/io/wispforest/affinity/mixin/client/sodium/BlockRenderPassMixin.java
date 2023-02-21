package io.wispforest.affinity.mixin.client.sodium;

import io.wispforest.affinity.client.render.SkyCaptureBuffer;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import net.minecraft.client.render.RenderLayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(BlockRenderPass.class)
public class BlockRenderPassMixin {

    @Invoker("<init>")
    public static BlockRenderPass sky$invokeNew(String internalName, int ordinal, RenderLayer layer, boolean translucent, float alphaCutoff) {
        throw new IllegalStateException("How did this mixin stub get called conc");
    }

    @Final
    @Shadow
    @Mutable
    private static BlockRenderPass[] $VALUES;

    @Inject(method = "<clinit>", at = @At(value = "FIELD", target = "Lme/jellysquid/mods/sodium/client/render/chunk/passes/BlockRenderPass;$VALUES:[Lme/jellysquid/mods/sodium/client/render/chunk/passes/BlockRenderPass;", shift = At.Shift.AFTER, opcode = Opcodes.PUTSTATIC))
    private static void addSkyStencilPass(CallbackInfo ci) {
        var blockPasses = new BlockRenderPass[$VALUES.length + 1];
        System.arraycopy($VALUES, 0, blockPasses, 0, $VALUES.length);

        blockPasses[blockPasses.length - 1] = sky$invokeNew("SKY_STENCIL", $VALUES.length, SkyCaptureBuffer.SKY_STENCIL_LAYER, false, 0);

        $VALUES = blockPasses;
    }

}
