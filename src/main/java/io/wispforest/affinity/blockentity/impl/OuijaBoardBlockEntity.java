package io.wispforest.affinity.blockentity.impl;

import io.wispforest.affinity.object.AffinityBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class OuijaBoardBlockEntity extends BlockEntity {

    @Environment(EnvType.CLIENT) public double time = 0d;
    @Environment(EnvType.CLIENT) public float boardHeight = 0f;

    public OuijaBoardBlockEntity(BlockPos pos, BlockState state) {
        super(AffinityBlocks.Entities.OUIJA_BOARD, pos, state);
    }
}
