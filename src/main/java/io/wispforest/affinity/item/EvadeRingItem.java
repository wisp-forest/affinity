package io.wispforest.affinity.item;

import dev.emi.trinkets.api.TrinketItem;
import dev.emi.trinkets.api.TrinketsApi;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.AffinityParticleSystems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EvadeRingItem extends TrinketItem {

    public static final float AETHUM_PER_USE = 1.5f;
    public static final int COOLDOWN_AFTER_USE = 5;

    public EvadeRingItem() {
        super(AffinityItems.settings().maxCount(1));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.affinity.evade_ring.tooltip.take_control", MathUtil.rounded(AETHUM_PER_USE, 1)));
        tooltip.add(Text.translatable("item.affinity.evade_ring.tooltip.consumption_per_use", MathUtil.rounded(AETHUM_PER_USE, 1)));
    }

    public record EvadePacket(Vec3d direction) {}

    static {
        AffinityNetwork.CHANNEL.registerServerbound(EvadePacket.class, (message, access) -> {
            if (!TrinketsApi.getTrinketComponent(access.player()).get().isEquipped(AffinityItems.EVADE_RING)) return;
            if (!access.player().getComponent(AffinityComponents.PLAYER_AETHUM).tryConsumeAethum(AETHUM_PER_USE)) return;

            access.player().getComponent(AffinityComponents.EVADE).evade(message.direction);
            access.player().getItemCooldownManager().set(AffinityItems.EVADE_RING, COOLDOWN_AFTER_USE);
            AffinityParticleSystems.EVADE.spawn(access.player().getWorld(), access.player().getPos().add(0, 1, 0), message.direction);
        });
    }
}
