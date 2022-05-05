package io.wispforest.affinity.client.render;

import io.wispforest.affinity.enchantment.template.AbsoluteEnchantment;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AbsoluteEnchantmentGlintHandler extends RenderLayer {

    private static final Map<AbsoluteEnchantment, List<RenderLayer>> LAYERS = new HashMap<>();

    private static AbsoluteEnchantment currentRenderEnchantment = null;

    private AbsoluteEnchantmentGlintHandler(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
        throw new IllegalStateException("This class should never ever be instantiated");
    }

    public static void createLayers() {
        Registry.ENCHANTMENT.stream()
                .filter(enchantment -> enchantment instanceof AbsoluteEnchantment)
                .map(AbsoluteEnchantment.class::cast)
                .forEach(enchantment -> {
                    final var id = Registry.ENCHANTMENT.getId(enchantment).getPath();
                    LAYERS.put(enchantment, makeGlintLayers(id.toLowerCase(Locale.ROOT), enchantment.nameHue()));
                });
    }

    public static void assignBuffers(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder> builderStorage) {
        LAYERS.forEach((absoluteEnchantment, renderLayers) -> {
            for (var layer : renderLayers) {
                builderStorage.put(layer, new BufferBuilder(layer.getExpectedBufferSize()));
            }
        });
    }

    public static void prepareGlintColor(ItemStack targetStack) {
        final var enchantments = EnchantmentHelper.get(targetStack);

        for (var enchantment : LAYERS.keySet()) {
            if (!enchantments.containsKey(enchantment)) continue;
            currentRenderEnchantment = enchantment;
            return;
        }

        currentRenderEnchantment = null;
    }

    public static void inject(CallbackInfoReturnable<RenderLayer> cir, int index) {
        if (currentRenderEnchantment == null) return;
        cir.setReturnValue(LAYERS.get(currentRenderEnchantment).get(index));
    }

    private static List<RenderLayer> makeGlintLayers(String name, int hue) {
        return List.of(
                makeGlintLayer(ARMOR_GLINT_SHADER, GLINT_TEXTURING, "armor_" + name, false, true, hue),
                makeGlintLayer(ARMOR_ENTITY_GLINT_SHADER, ENTITY_GLINT_TEXTURING, "armor_entity_" + name, false, true, hue),
                makeGlintLayer(TRANSLUCENT_GLINT_SHADER, GLINT_TEXTURING, "translucent" + name, true, false, hue),
                makeGlintLayer(GLINT_SHADER, GLINT_TEXTURING, "normal" + name, false, false, hue),
                makeGlintLayer(DIRECT_GLINT_SHADER, GLINT_TEXTURING, "direct" + name, false, false, hue),
                makeGlintLayer(ENTITY_GLINT_SHADER, ENTITY_GLINT_TEXTURING, "entity" + name, true, false, hue),
                makeGlintLayer(DIRECT_ENTITY_GLINT_SHADER, ENTITY_GLINT_TEXTURING, "direct_entity" + name, false, false, hue)
        );
    }

    private static RenderLayer makeGlintLayer(RenderPhase.Shader shader, RenderPhase.Texturing texturing, String name, boolean itemTarget, boolean layered, int hue) {
        final var parameters = MultiPhaseParameters.builder()
                .shader(shader)
                .texture(new AbsoluteEnchantmentGlintTexture(hue))
                .writeMaskState(COLOR_MASK)
                .cull(DISABLE_CULLING)
                .depthTest(EQUAL_DEPTH_TEST)
                .transparency(GLINT_TRANSPARENCY)
                .texturing(texturing);

        if (itemTarget) parameters.target(ITEM_TARGET);
        if (layered) parameters.layering(VIEW_OFFSET_Z_LAYERING);

        return RenderLayer.of(
                name + "_glint",
                VertexFormats.POSITION_TEXTURE,
                VertexFormat.DrawMode.QUADS,
                256,
                parameters.build(false)
        );
    }

}
