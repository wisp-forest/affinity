package io.wispforest.affinity.compat.emi;

import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Collectors;

public class StatusEffectEmiStack extends EmiStack {

    private final StatusEffect effect;
    public StatusEffectEmiStack(StatusEffect effect) {
        this.effect = effect;
    }

    @Override
    public EmiStack copy() {
        return this;
    }

    @Override
    public void render(DrawContext draw, int x, int y, float delta, int flags) {
        var sprite = MinecraftClient.getInstance().getStatusEffectSpriteManager().getSprite(this.effect);
        draw.drawSprite(x - 1, y - 1, 0, 18, 18, sprite);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public NbtCompound getNbt() {
        return null;
    }

    @Override
    public Object getKey() {
        return this.effect;
    }

    @Override
    public Identifier getId() {
        return Registries.STATUS_EFFECT.getId(this.effect);
    }

    @Override
    public List<Text> getTooltipText() {
        return List.of(this.effect.getName());
    }

    @Override
    public List<TooltipComponent> getTooltip() {
        var tooltip = getTooltipText().stream().map(EmiTooltipComponents::of).collect(Collectors.toList());
        if (this.amount > 1) {
            tooltip.add(EmiTooltipComponents.getAmount(this));
        }

        EmiTooltipComponents.appendModName(
                tooltip,
                Registries.STATUS_EFFECT.getId(this.effect).getNamespace()
        );

        tooltip.addAll(super.getTooltip());
        return tooltip;
    }

    @Override
    public Text getName() {
        return this.effect.getName();
    }
}
