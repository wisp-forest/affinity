package com.glisco.nidween;

import com.glisco.nidween.registries.NidweenBlocks;
import com.glisco.nidween.registries.NidweenStatusEffects;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class Nidween implements ModInitializer {

    public static final String MOD_ID = "nidween";

    @Override
    public void onInitialize() {
        NidweenBlocks.register();
        NidweenStatusEffects.register();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

}
