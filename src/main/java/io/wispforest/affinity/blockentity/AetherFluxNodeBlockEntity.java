package io.wispforest.affinity.blockentity;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.registries.AffinityBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class AetherFluxNodeBlockEntity extends AetherNetworkMemberBlockEntity implements AetherNetworkNode {

    public AetherFluxNodeBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.AETHER_FLUX_NODE, pos, state);
    }

    @Override
    public AetherLink.Result createGenericLink(BlockPos pos) {
        if (isLinked(pos)) return AetherLink.Result.ALREADY_LINKED;

        var member = Affinity.AETHER_MEMBER.find(world, pos, null);
        if (member == null) return AetherLink.Result.NO_TARGET;

        if (member instanceof AetherNetworkNode node) {
            if (node.isLinked(this.pos)) return AetherLink.Result.ALREADY_LINKED;
            node.addNodeLink(this.pos);
        } else {
            if (!member.addLinkParent(this.pos)) return AetherLink.Result.ALREADY_LINKED;
        }

        this.LINKED_MEMBERS.add(pos);
        this.markDirty();

        return AetherLink.Result.SUCCESS;
    }

    @Override
    public void addNodeLink(BlockPos pos) {
        this.LINKED_MEMBERS.add(pos);
        this.markDirty();
    }

    @Override
    public boolean canLink(BlockPos pos) {
        return !isLinked(pos);
    }
}
