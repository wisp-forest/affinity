package io.wispforest.affinity.mixin.client.sodium;

import io.wispforest.affinity.misc.CompatMixin;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.attributes.CommonVertexAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.*;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import net.minecraft.client.render.FixedColorVertexConsumer;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@CompatMixin("sodium")
@Mixin(targets = "io.wispforest.affinity.client.render.EmancipationVertexConsumerProvider$AlphaMaskConsumer", remap = false)
public abstract class AlphaMaskConsumerMixin extends FixedColorVertexConsumer implements VertexBufferWriter {

    @Shadow
    @Final
    private Vector3f pos;
    @Shadow
    @Final
    private Vector3f normal;
    @Shadow
    @Final
    private Vector2f texture;
    @Shadow
    private int light;

    @Override
    public void push(MemoryStack memoryStack, long srcBuffer, int vtxCount, VertexFormatDescription format) {
        for (int i = 0; i < vtxCount; i++) {
            long elementIdx = srcBuffer + (long) i * format.stride();

            for (var element : CommonVertexAttribute.values()) {
                if (!format.containsElement(element)) continue;

                switch (element) {
                    case POSITION -> this.pos.set(PositionAttribute.getX(elementIdx), PositionAttribute.getY(elementIdx), PositionAttribute.getZ(elementIdx));
                    case COLOR -> this.color(ColorAttribute.get(elementIdx));
                    case TEXTURE -> this.texture.set(TextureAttribute.get(elementIdx));
                    case LIGHT -> this.light = LightAttribute.get(elementIdx);
                    case NORMAL -> {
                        var normal = NormalAttribute.get(elementIdx);
                        this.normal.set(NormI8.unpackX(normal), NormI8.unpackY(normal), NormI8.unpackZ(normal));
                    }
                }

                elementIdx += element.getByteLength();
            }

            this.next();
        }
    }
}
