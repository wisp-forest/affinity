package io.wispforest.affinity.compat.owowhatsthis;

import io.wispforest.affinity.Affinity;
import io.wispforest.owowhatsthis.NumberFormatter;
import io.wispforest.owowhatsthis.OwoWhatsThis;
import io.wispforest.owowhatsthis.client.DisplayAdapters;
import io.wispforest.owowhatsthis.compat.OwoWhatsThisPlugin;
import io.wispforest.owowhatsthis.information.BlockStateWithPosition;
import io.wispforest.owowhatsthis.information.InformationProvider;
import io.wispforest.owowhatsthis.information.InformationProviders;
import io.wispforest.owowhatsthis.information.TargetType;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

public class AffinityOwoWhatsThisPlugin implements OwoWhatsThisPlugin {

    @Override
    public void loadServer() {
        Registry.register(OwoWhatsThis.INFORMATION_PROVIDER, Affinity.id("block_aethum_flux_storage"), BLOCK_AETHUM_FLUX_STORAGE);
    }

    @Override
    public void loadClient() {
        DisplayAdapters.register(BLOCK_AETHUM_FLUX_STORAGE, InformationProviders.DisplayAdapters.TEXT);
    }

    public static final InformationProvider<BlockStateWithPosition, Text> BLOCK_AETHUM_FLUX_STORAGE = InformationProvider.server(
            TargetType.BLOCK,
            true, 0, Text.class,
            (player, world, target) -> {
                var member = Affinity.AETHUM_MEMBER.find(world, target.pos(), null);
                if (member == null) return null;

                return Text.literal(
                        "Aethum Flux: "
                                + NumberFormatter.quantity(member.flux(), "AF")
                                + "/"
                                + NumberFormatter.quantity(member.fluxCapacity(), "AF")
                );
            }
    );
}
