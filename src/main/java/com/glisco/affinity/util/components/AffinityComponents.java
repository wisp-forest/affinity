package com.glisco.affinity.util.components;

import com.glisco.affinity.Affinity;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;

public class AffinityComponents implements EntityComponentInitializer {

    public static final ComponentKey<com.glisco.affinity.util.components.GlowingColorComponent> GLOWING_COLOR = ComponentRegistry.getOrCreate(Affinity.id("glowing_color"), com.glisco.affinity.util.components.GlowingColorComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(GLOWING_COLOR, com.glisco.affinity.util.components.GlowingColorComponent::new, RespawnCopyStrategy.NEVER_COPY);
    }
}
