package io.wispforest.affinity.mixin.client.sodium;

import io.wispforest.affinity.client.render.SkyCaptureBuffer;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPassManager;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Pseudo
@Mixin(value = BlockRenderPassManager.class, remap = false)
public abstract class BlockRenderPassManagerMixin {

    @Shadow
    protected abstract void addMapping(RenderLayer layer, BlockRenderPass type);

    @Inject(method = "createDefaultMappings", at = @At("TAIL"))
    private static void injectSkyStencil(CallbackInfoReturnable<BlockRenderPassManager> cir) {
        BlockRenderPass skyStencil = Arrays.stream(BlockRenderPass.VALUES)
                .filter(blockRenderPass -> blockRenderPass.name().equals("SKY_STENCIL"))
                .findAny()
                .orElse(null);

        ((BlockRenderPassManagerMixin) (Object) cir.getReturnValue()).addMapping(SkyCaptureBuffer.SKY_STENCIL_LAYER, skyStencil);
    }

}
