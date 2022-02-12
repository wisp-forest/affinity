package io.wispforest.affinity.block.impl;

import io.wispforest.affinity.block.template.AethumNetworkMemberBlock;
import io.wispforest.affinity.blockentity.impl.RitualCoreBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class AspRiteCoreBlock extends AethumNetworkMemberBlock {

    public AspRiteCoreBlock() {
        super(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).nonOpaque());
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RitualCoreBlockEntity(pos, state);
    }
}
