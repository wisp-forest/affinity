package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.registration.annotations.AssignedName;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.registry.Registry;

public class AffinitySoundEvents implements AutoRegistryContainer<SoundEvent> {

    @AssignedName("block.peculiar_clump.resonance")
    public static final SoundEvent BLOCK_PECULIAR_CLUMP_RESONANCE = new SoundEvent(Affinity.id("block.peculiar_clump.resonance"));

    @Override
    public Registry<SoundEvent> getRegistry() {
        return Registry.SOUND_EVENT;
    }

    @Override
    public Class<SoundEvent> getTargetFieldType() {
        return SoundEvent.class;
    }
}
