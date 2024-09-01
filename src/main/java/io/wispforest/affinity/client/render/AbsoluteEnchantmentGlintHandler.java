package io.wispforest.affinity.client.render;

import io.wispforest.affinity.object.AffinityEnchantmentEffectComponents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Consumer;

public class AbsoluteEnchantmentGlintHandler extends RenderLayer {

    private static final Map<RegistryEntry<Enchantment>, List<RenderLayer>> LAYERS = new HashMap<>();

    private static RegistryEntry<Enchantment> currentRenderEnchantment = null;

    private static Consumer<RenderLayer> clearAssignment = null;
    private static Consumer<RenderLayer> assignBuffer = null;

    private AbsoluteEnchantmentGlintHandler(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
        throw new IllegalStateException("This class should never ever be instantiated");
    }

    public static void setupCallbacks(Consumer<RenderLayer> clearAssignment, Consumer<RenderLayer> assignBuffer) {
        AbsoluteEnchantmentGlintHandler.clearAssignment = clearAssignment;
        AbsoluteEnchantmentGlintHandler.assignBuffer = assignBuffer;
    }

    public static void reloadLayers(RegistryWrapper.WrapperLookup registries) {
        LAYERS.forEach((enchantmentRegistryEntry, renderLayers) -> {
            renderLayers.forEach(clearAssignment);
        });

        LAYERS.clear();

        var registry = registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        registry.streamEntries()
                .filter(entry -> entry.value().effects().contains(AffinityEnchantmentEffectComponents.ABSOLUTE_NAME_HUE))
                .forEach(entry -> {
                    final var id = entry.registryKey().getValue().getPath();
                    final var layers = makeGlintLayers(id.toLowerCase(Locale.ROOT), entry.value().effects().get(AffinityEnchantmentEffectComponents.ABSOLUTE_NAME_HUE));

                    layers.forEach(assignBuffer);
                    LAYERS.put(entry, layers);
                });
    }

    public static void prepareGlintColor(ItemStack targetStack) {
        final var enchantments = EnchantmentHelper.getEnchantments(targetStack);

        for (var enchantment : LAYERS.keySet()) {
            if (!enchantments.getEnchantments().contains(enchantment)) continue;
            currentRenderEnchantment = enchantment;
            return;
        }

        currentRenderEnchantment = null;
    }

    public static void inject(CallbackInfoReturnable<RenderLayer> cir, int index) {
        if (currentRenderEnchantment == null) return;
        if (!LAYERS.containsKey(currentRenderEnchantment) || index >= LAYERS.get(currentRenderEnchantment).size()) return;

        cir.setReturnValue(LAYERS.get(currentRenderEnchantment).get(index));
    }

    private static List<RenderLayer> makeGlintLayers(String name, int hue) {
        return List.of(
                makeGlintLayer(ARMOR_ENTITY_GLINT_PROGRAM, ENTITY_GLINT_TEXTURING, "armor_entity_" + name, false, true, hue),
                makeGlintLayer(TRANSLUCENT_GLINT_PROGRAM, GLINT_TEXTURING, "translucent" + name, true, false, hue),
                makeGlintLayer(GLINT_PROGRAM, GLINT_TEXTURING, "normal" + name, false, false, hue),
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
