package io.wispforest.affinity.mixin.client.sodium;

import io.wispforest.affinity.misc.CompatMixin;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.*;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static net.minecraft.client.render.VertexFormatElement.*;

@CompatMixin("sodium")
@Mixin(targets = "io.wispforest.affinity.client.render.EmancipationVertexConsumerProvider$AlphaMaskConsumer", remap = false)
public abstract class AlphaMaskConsumerMixin implements VertexConsumer, VertexBufferWriter {

    @Shadow
    @Final
    private Vector3f pos;
    @Shadow
    @Final
    private Vector2f texture;
    @Shadow
    private int light;

    @Override
    public void push(MemoryStack memoryStack, long srcBuffer, int vtxCount, VertexFormat format) {
        for (int i = 0; i < vtxCount; i++) {
            long elementIdx = srcBuffer + (long) i * format.getVertexSizeByte();
            var elementNormal = new Vector3f();

            for (var element : format.getElements()) {
                if (element.equals(POSITION)) {
                    this.pos.set(PositionAttribute.getX(elementIdx), PositionAttribute.getY(elementIdx), PositionAttribute.getZ(elementIdx));
                } else if (element.equals(COLOR)) {
                    this.color(ColorAttribute.get(elementIdx));
                } else if (element.equals(UV_0)) {
                    this.texture.set(TextureAttribute.get(elementIdx));
                } else if (element.equals(UV_1)) {
                    this.overlay(OverlayAttribute.get(elementIdx));
                } else if (element.equals(UV_2)) {
                    this.light = LightAttribute.get(elementIdx);
                } else if (element.equals(NORMAL)) {
                    var normal = NormalAttribute.get(elementIdx);
                    elementNormal.set(NormI8.unpackX(normal), NormI8.unpackY(normal), NormI8.unpackZ(normal));
                } else {
                    throw new IllegalStateException("Unable to handle the given VertexFormats Element type: " + element);
                }

                elementIdx += element.getSizeInBytes();
            }

            this.normal(elementNormal.x, elementNormal.y, elementNormal.z);
        }
    }
}
