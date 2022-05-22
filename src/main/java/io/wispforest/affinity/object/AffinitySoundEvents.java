package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.registration.annotations.AssignedName;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.registry.Registry;

public class AffinitySoundEvents implements AutoRegistryContainer<SoundEvent> {

    @AssignedName("block.peculiar_clump.resonance")
    public static final SoundEvent BLOCK_PECULIAR_CLUMP_RESONANCE = new SoundEvent(Affinity.id("block.peculiar_clump.resonance"));

    @AssignedName("block.ritual_socle.activate")
    public static final SoundEvent BLOCK_RITUAL_SOCLE_ACTIVATE = new SoundEvent(Affinity.id("block.ritual_socle.activate"));

    @AssignedName("block.aberrant_calling_core.ritual_success")
    public static final SoundEvent BLOCK_ABERRANT_CALLING_CORE_RITUAL_SUCCESS = new SoundEvent(Affinity.id("block.aberrant_calling_core.ritual_success"));

    @Override
    public Registry<SoundEvent> getRegistry() {
        return Registry.SOUND_EVENT;
    }

    @Override
    public Class<SoundEvent> getTargetFieldType() {
        return SoundEvent.class;
    }
}
