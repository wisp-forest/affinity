package io.wispforest.affinity.compat.emi;

import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.entity.effect.StatusEffect;

public class StatusEffectSlotWidget extends SlotWidget {

    public StatusEffectSlotWidget(StatusEffect effect, int x, int y) {
        super(new StatusEffectEmiStack(effect), x, y);
    }

    @Override
    public boolean shouldDrawSlotHighlight(int mouseX, int mouseY) {
        return false;
    }
}
