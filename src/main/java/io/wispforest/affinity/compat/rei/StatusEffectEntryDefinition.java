package io.wispforest.affinity.compat.rei;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.util.RegistryAccess;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntrySerializer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.stream.Stream;

public class StatusEffectEntryDefinition implements EntryDefinition<StatusEffect>, EntrySerializer<StatusEffect> {
    @Override
    public Class<StatusEffect> getValueType() {
        return StatusEffect.class;
    }

    @Override
    public EntryType<StatusEffect> getType() {
        return AffinityReiCommonPlugin.EFFECT_ENTRY_TYPE;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public EntryRenderer<StatusEffect> getRenderer() {
        return Renderer.INSTANCE;
    }

    @Override
    public @Nullable Identifier getIdentifier(EntryStack<StatusEffect> entry, StatusEffect value) {
        return Registries.STATUS_EFFECT.getId(value);
    }

    @Override
    public boolean isEmpty(EntryStack<StatusEffect> entry, StatusEffect value) {
        return false;
    }

    @Override
    public StatusEffect copy(EntryStack<StatusEffect> entry, StatusEffect value) {
        return value;
    }

    @Override
    public StatusEffect normalize(EntryStack<StatusEffect> entry, StatusEffect value) {
        return value;
    }

    @Override
    public StatusEffect wildcard(EntryStack<StatusEffect> entry, StatusEffect value) {
        return value;
    }

    @Override
    public long hash(EntryStack<StatusEffect> entry, StatusEffect value, ComparisonContext context) {
        return value.hashCode();
    }

    @Override
    public boolean equals(StatusEffect o1, StatusEffect o2, ComparisonContext context) {
        return o1 == o2;
    }

    @Override
    public @Nullable EntrySerializer<StatusEffect> getSerializer() {
        return this;
    }

    @Override
    public Text asFormattedText(EntryStack<StatusEffect> entry, StatusEffect value) {
        return value.getName();
    }

    @Override
    public Stream<? extends TagKey<?>> getTagsFor(EntryStack<StatusEffect> entry, StatusEffect value) {
        return RegistryAccess.getEntry(Registries.STATUS_EFFECT, value).streamTags();
    }

    @Override
    public boolean supportSaving() {
        return true;
    }

    @Override
    public boolean supportReading() {
        return true;
    }

    @Override
    public NbtCompound save(EntryStack<StatusEffect> entry, StatusEffect value) {
        NbtCompound mald = new NbtCompound();

        mald.putString("id", String.valueOf(Registries.STATUS_EFFECT.getId(value)));

        return mald;
    }

    @Override
    public StatusEffect read(NbtCompound tag) {
        return Registries.STATUS_EFFECT.get(new Identifier(tag.getString("id")));
    }

    @Environment(EnvType.CLIENT)
    private static class Renderer implements EntryRenderer<StatusEffect> {
        private static final Renderer INSTANCE = new Renderer();

        @Override
        public void render(EntryStack<StatusEffect> entry, MatrixStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
            var sprite = MinecraftClient.getInstance().getStatusEffectSpriteManager().getSprite(entry.getValue());

            RenderSystem.setShaderTexture(0, sprite.getAtlasId());
            Drawer.drawSprite(matrices, bounds.x - 1, bounds.y - 1, 0, bounds.width + 2, bounds.height + 2, sprite);
        }

        @Override
        public @Nullable Tooltip getTooltip(EntryStack<StatusEffect> entry, TooltipContext context) {
            var tooltip = new ArrayList<Text>();
            tooltip.add(entry.getValue().getName());

            if (MinecraftClient.getInstance().options.advancedItemTooltips) {
                tooltip.add(Text.literal(Registries.STATUS_EFFECT.getId(entry.getValue()).toString()).formatted(Formatting.DARK_GRAY));
            }

            return Tooltip.create(tooltip);
        }
    }
}
