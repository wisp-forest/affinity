package io.wispforest.affinity.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class AbsoluteEnchantmentGlintHandler extends RenderLayer {

    private static final Map<RegistryEntry<Enchantment>, List<RenderLayer>> LAYERS = new HashMap<>();

    private static RegistryEntry<Enchantment> currentRenderEnchantment = null;

    private AbsoluteEnchantmentGlintHandler(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
        throw new IllegalStateException("This class should never ever be instantiated");
    }

    public static void createLayers() {
        // TODO: this needs to be moved to some place after dynamic registry init

//        Registries.ENCHANTMENT.stream()
//                .filter(enchantment -> enchantment instanceof AbsoluteEnchantment)
//                .map(AbsoluteEnchantment.class::cast)
//                .forEach(enchantment -> {
//                    final var id = Registries.ENCHANTMENT.getId(enchantment).getPath();
//                    LAYERS.put(enchantment, makeGlintLayers(id.toLowerCase(Locale.ROOT), enchantment.nameHue()));
//                });
    }

    public static void assignBuffers(Consumer<RenderLayer> bufferMaker) {
        LAYERS.forEach((absoluteEnchantment, renderLayers) -> {
            for (var layer : renderLayers) {
                bufferMaker.accept(layer);
            }
        });
    }

    public static void prepareGlintColor(ItemStack targetStack) {
        final var enchantments = targetStack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

        for (var enchantment : LAYERS.keySet()) {
            if (!enchantments.getEnchantments().contains(enchantment)) continue;
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
        // TODO: investigate where these programs actually went

        return List.of(
//                makeGlintLayer(ARMOR_GLINT_PROGRAM, GLINT_TEXTURING, "armor_" + name, false, true, hue),
                makeGlintLayer(ARMOR_ENTITY_GLINT_PROGRAM, ENTITY_GLINT_TEXTURING, "armor_entity_" + name, false, true, hue),
                makeGlintLayer(TRANSLUCENT_GLINT_PROGRAM, GLINT_TEXTURING, "translucent" + name, true, false, hue),
                makeGlintLayer(GLINT_PROGRAM, GLINT_TEXTURING, "normal" + name, false, false, hue),
//                makeGlintLayer(DIRECT_GLINT_PROGRAM, GLINT_TEXTURING, "direct" + name, false, false, hue),
                makeGlintLayer(ENTITY_GLINT_PROGRAM, ENTITY_GLINT_TEXTURING, "entity" + name, true, false, hue),
                makeGlintLayer(DIRECT_ENTITY_GLINT_PROGRAM, ENTITY_GLINT_TEXTURING, "direct_entity" + name, false, false, hue)
        );
    }

    private static RenderLayer makeGlintLayer(RenderPhase.ShaderProgram shaderProgram, RenderPhase.Texturing texturing, String name, boolean itemTarget, boolean layered, int hue) {
        final var parameters = MultiPhaseParameters.builder()
                .program(shaderProgram)
                .texture(new AbsoluteEnchantmentGlintTexture(hue))
                .writeMaskState(COLOR_MASK)
                .cull(DISABLE_CULLING)
                .depthTest(EQUAL_DEPTH_TEST)
                .transparency(GLINT_TRANSPARENCY)
                .texturing(texturing);

        if (itemTarget) parameters.target(ITEM_ENTITY_TARGET);
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
