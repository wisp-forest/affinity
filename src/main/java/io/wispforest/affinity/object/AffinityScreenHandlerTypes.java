package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.misc.screenhandler.RitualSocleComposerScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;

public class AffinityScreenHandlerTypes {

    public static final ScreenHandlerType<RitualSocleComposerScreenHandler> RITUAL_SOCLE_COMPOSER =
            ScreenHandlerRegistry.registerSimple(Affinity.id("ritual_socle_composer"), RitualSocleComposerScreenHandler::client);

    public static void initialize() {}
}
