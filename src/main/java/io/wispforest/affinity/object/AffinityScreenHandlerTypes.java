package io.wispforest.affinity.object;

import io.wispforest.affinity.misc.screenhandler.AssemblyAugmentScreenHandler;
import io.wispforest.affinity.misc.screenhandler.RitualSocleComposerScreenHandler;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.registry.Registry;

public class AffinityScreenHandlerTypes implements AutoRegistryContainer<ScreenHandlerType<?>> {

    public static final ScreenHandlerType<RitualSocleComposerScreenHandler> RITUAL_SOCLE_COMPOSER = new ScreenHandlerType<>(RitualSocleComposerScreenHandler::client);
    public static final ScreenHandlerType<AssemblyAugmentScreenHandler> ASSEMBLY_AUGMENT = new ScreenHandlerType<>(AssemblyAugmentScreenHandler::client);

    @Override
    public Registry<ScreenHandlerType<?>> getRegistry() {
        return Registry.SCREEN_HANDLER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<ScreenHandlerType<?>> getTargetFieldType() {
        return (Class<ScreenHandlerType<?>>) (Object) ScreenHandlerType.class;
    }
}
