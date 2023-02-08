package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class ArborealAccumulationApparatusBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity {

    private int generationFactor = -1;

    public ArborealAccumulationApparatusBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ARBOREAL_ACCUMULATION_APPARATUS, pos, state);

        this.fluxStorage.setFluxCapacity(8000);
        this.fluxStorage.setMaxExtract(250);
    }

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        super.appendTooltipEntries(entries);
        entries.add(Entry.icon(Text.of(this.generationFactor + "/t"), 8, 0));
    }

    @Override
    public void tickServer() {
        if (this.generationFactor == -1 || this.world.getTime() % 200 == 0) this.updateGenerationFactor();

        var flux = this.fluxStorage.flux();
        if (flux < this.fluxStorage.fluxCapacity()) {
            this.updateFlux(Math.min(flux + generationFactor, this.fluxStorage.fluxCapacity()));
        }
    }

    private void updateGenerationFactor() {
        this.generationFactor = 0;
        for (BlockPos pos : BlockPos.iterate(this.getPos().add(-5, -5, -5), this.pos.add(5, 5, 5))) {
            final var state = world.getBlockState(pos);
            if (!state.isIn(BlockTags.FLOWERS) && !state.isIn(BlockTags.LEAVES)) continue;
            this.generationFactor++;
        }

        AffinityNetwork.CHANNEL.serverHandle(PlayerLookup.tracking(this)).send(new GenerationFactorPacket(this.pos, this.generationFactor));
    }

    static {
        AffinityNetwork.CHANNEL.registerClientbound(GenerationFactorPacket.class, (message, access) -> {
            if (!(access.runtime().world.getBlockEntity(message.pos()) instanceof ArborealAccumulationApparatusBlockEntity apparatus)) return;
            apparatus.generationFactor = message.factor();
        });
    }

    public record GenerationFactorPacket(BlockPos pos, Integer factor) {}
}
