package io.wispforest.affinity.compat.owowhatsthis;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owowhatsthis.NumberFormatter;
import io.wispforest.owowhatsthis.OwoWhatsThis;
import io.wispforest.owowhatsthis.client.DisplayAdapters;
import io.wispforest.owowhatsthis.client.component.ColoredProgressBarComponent;
import io.wispforest.owowhatsthis.compat.OwoWhatsThisPlugin;
import io.wispforest.owowhatsthis.information.BlockStateWithPosition;
import io.wispforest.owowhatsthis.information.InformationProvider;
import io.wispforest.owowhatsthis.information.TargetType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public class AffinityOwoWhatsThisPlugin implements OwoWhatsThisPlugin {

    @Override
    public void loadServer() {
        Registry.register(OwoWhatsThis.INFORMATION_PROVIDER, Affinity.id("block_aethum_flux_storage"), BLOCK_AETHUM_FLUX_STORAGE);
    }

    @Override
    public void loadClient() {
        DisplayAdapters.register(BLOCK_AETHUM_FLUX_STORAGE, Client.AETHUM_STORAGE);
    }

    public static final InformationProvider<BlockStateWithPosition, AethumStorageData> BLOCK_AETHUM_FLUX_STORAGE = InformationProvider.server(
            TargetType.BLOCK,
            true, 0, AethumStorageData.class,
            (player, world, target) -> {
                var member = Affinity.AETHUM_MEMBER.find(world, target.pos(), null);
                if (member == null) return null;

                return new AethumStorageData(member.flux(), member.fluxCapacity());
            }
    );

    public record AethumStorageData(long stored, long capacity) {}

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static final InformationProvider.DisplayAdapter<AethumStorageData> AETHUM_STORAGE = data -> {
            final var fuelText = Text.translatable(
                    "text.affinity.tooltip.aethum_storage",
                    NumberFormatter.quantityText(data.stored, ""),
                    NumberFormatter.quantityText(data.capacity, "")
            );

            return new ColoredProgressBarComponent(fuelText)
                    .progress(data.stored / (float) data.capacity)
                    .color(Color.ofRgb(Affinity.AETHUM_FLUX_COLOR));
        };
    }
}
