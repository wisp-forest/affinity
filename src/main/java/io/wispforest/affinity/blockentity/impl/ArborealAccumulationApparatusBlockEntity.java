package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.blockentity.template.TickedBlockEntity;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArborealAccumulationApparatusBlockEntity extends AethumNetworkMemberBlockEntity implements TickedBlockEntity, InquirableOutlineProvider {

    private int generationFactor = -1;
    private int time = 0;

    public ArborealAccumulationApparatusBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.ARBOREAL_ACCUMULATION_APPARATUS, pos, state);

        this.fluxStorage.setFluxCapacity(8000);
        this.fluxStorage.setMaxExtract(250);
    }

    @Override
    public void appendTooltipEntries(List<Entry> entries) {
        super.appendTooltipEntries(entries);
        entries.add(Entry.icon(Text.translatable(this.getCachedState().getBlock().getTranslationKey() + ".generation_rate_tooltip", this.generationFactor, 10), 8, 0));
    }

    @Override
    public void tickServer() {
        if (this.generationFactor == -1 || this.world.getTime() % 200 == 0) this.updateGenerationFactor();
        this.time++;

        if (this.time % 200 != 0) return;

        var flux = this.fluxStorage.flux();
        if (flux < this.fluxStorage.fluxCapacity()) {
            this.updateFlux(Math.min(flux + generationFactor, this.fluxStorage.fluxCapacity()));
        }
    }

    private void updateGenerationFactor() {
        this.generationFactor = 0;

        for (var pos : BlockPos.iterate(this.getPos().add(-5, -5, -5), this.pos.add(5, 5, 5))) {
            if (this.generationFactor >= 200) break;

            final var state = world.getBlockState(pos);
            if (state.isIn(BlockTags.FLOWERS)) {
                this.generationFactor += 3;
                continue;
            }

            if (state.isIn(BlockTags.LEAVES) && !(state.getBlock() instanceof LeavesBlock && state.get(LeavesBlock.PERSISTENT))) {
                this.generationFactor++;
            }
        }

        this.generationFactor = Math.min(this.generationFactor, 200);
        AffinityNetwork.CHANNEL.serverHandle(PlayerLookup.tracking(this)).send(new GenerationFactorPacket(this.pos, this.generationFactor));
    }

    @Override
    public @Nullable CuboidRenderer.Cuboid getActiveOutline() {
        return CuboidRenderer.Cuboid.symmetrical(5, 5, 5);
    }

    static {
        AffinityNetwork.CHANNEL.registerClientbound(GenerationFactorPacket.class, (message, access) -> {
            if (!(access.runtime().world.getBlockEntity(message.pos()) instanceof ArborealAccumulationApparatusBlockEntity apparatus)) return;
            apparatus.generationFactor = message.factor();
        });
    }

    public record GenerationFactorPacket(BlockPos pos, int factor) {}
}
