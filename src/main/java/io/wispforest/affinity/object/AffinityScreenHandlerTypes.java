package io.wispforest.affinity.object;

import io.wispforest.affinity.misc.screenhandler.*;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

public class AffinityScreenHandlerTypes implements AutoRegistryContainer<ScreenHandlerType<?>> {

    public static final ScreenHandlerType<RitualSocleComposerScreenHandler> RITUAL_SOCLE_COMPOSER = new ScreenHandlerType<>(RitualSocleComposerScreenHandler::client, FeatureFlags.DEFAULT_ENABLED_FEATURES);
    public static final ScreenHandlerType<AssemblyAugmentScreenHandler> ASSEMBLY_AUGMENT = new ScreenHandlerType<>(AssemblyAugmentScreenHandler::client, FeatureFlags.DEFAULT_ENABLED_FEATURES);
    public static final ScreenHandlerType<OuijaBoardScreenHandler> OUIJA_BOARD = new ScreenHandlerType<>(OuijaBoardScreenHandler::client, FeatureFlags.DEFAULT_ENABLED_FEATURES);
    public static final ScreenHandlerType<ItemTransferNodeScreenHandler> ITEM_TRANSFER_NODE = new ScreenHandlerType<>(ItemTransferNodeScreenHandler::client, FeatureFlags.DEFAULT_ENABLED_FEATURES);
    public static final ScreenHandlerType<LargeAzaleaChestScreenHandler> LARGE_AZALEA_CHEST = new ScreenHandlerType<>(LargeAzaleaChestScreenHandler::client, FeatureFlags.DEFAULT_ENABLED_FEATURES);

    @Override
    public Registry<ScreenHandlerType<?>> getRegistry() {
        return Registries.SCREEN_HANDLER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<ScreenHandlerType<?>> getTargetFieldType() {
        return (Class<ScreenHandlerType<?>>) (Object) ScreenHandlerType.class;
    }
}
