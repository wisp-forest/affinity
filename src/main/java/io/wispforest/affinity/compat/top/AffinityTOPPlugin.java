package io.wispforest.affinity.compat.top;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.owo.ops.TextOps;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.elements.ElementProgress;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class AffinityTOPPlugin implements ITheOneProbePlugin {
    @Override
    public void onLoad(ITheOneProbe apiInstance) {
        apiInstance.registerProvider(new IProbeInfoProvider() {
            @Override
            public Identifier getID() {
                return Affinity.id("top_plugin");
            }

            @Override
            public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
                var member = Affinity.AETHUM_MEMBER.find(world, data.getPos(), null);
                if (member == null) return;

                final var flux = member instanceof AethumNetworkMemberBlockEntity entity ? entity.visualFlux() : member.flux();

                apiInstance.getStyleManager().progressStyleLife();

                final var suffix = TextOps.concat(Text.of(" / "), ElementProgress.format(member.fluxCapacity(), NumberFormat.COMPACT, Text.of("")));
                probeInfo.progress(flux, member.fluxCapacity(),
                        probeInfo.defaultProgressStyle()
                                .filledColor(0xff6A67CE)
                                .alternateFilledColor(0xae6A67CE)
                                .numberFormat(NumberFormat.COMPACT)
                                .suffix(suffix));
            }
        });
    }
}
