package io.wispforest.affinity.compat.emi;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.entity.effect.StatusEffect;

public class StatusEffectSlotWidget extends AffinitySlotWidget {

    public StatusEffectSlotWidget(StatusEffect effect, int x, int y) {
        super(new StatusEffectEmiStack(effect), x, y);
    }

    public StatusEffectSlotWidget(EmiStack effect, int x, int y) {
        super(effect, x, y);
    }

    @Override
    public boolean shouldDrawSlotHighlight(int mouseX, int mouseY) {
        return false;
    }
}
