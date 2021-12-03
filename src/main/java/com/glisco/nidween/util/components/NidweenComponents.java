package com.glisco.nidween.util.components;

import com.glisco.nidween.Nidween;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;

public class NidweenComponents implements EntityComponentInitializer {

    public static final ComponentKey<GlowingColorComponent> GLOWING_COLOR = ComponentRegistry.getOrCreate(Nidween.id("glowing_color"), GlowingColorComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(GLOWING_COLOR, GlowingColorComponent::new, RespawnCopyStrategy.NEVER_COPY);
    }
}
