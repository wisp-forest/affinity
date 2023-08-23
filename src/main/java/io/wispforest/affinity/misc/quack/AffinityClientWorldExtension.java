package io.wispforest.affinity.misc.quack;

import net.minecraft.util.math.ChunkSectionPos;

public interface AffinityClientWorldExtension {
    void affinity$addChunkSectionListener(ChunkSectionPos pos, Runnable listener);
}
