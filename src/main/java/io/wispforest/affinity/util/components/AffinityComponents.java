package io.wispforest.affinity.util.components;

import io.wispforest.affinity.Affinity;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;

public class AffinityComponents implements EntityComponentInitializer {

    public static final ComponentKey<GlowingColorComponent> GLOWING_COLOR = ComponentRegistry.getOrCreate(Affinity.id("glowing_color"), GlowingColorComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(GLOWING_COLOR, GlowingColorComponent::new, RespawnCopyStrategy.NEVER_COPY);
    }
}
