package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.registration.annotations.AssignedName;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

public class AffinitySoundEvents implements AutoRegistryContainer<SoundEvent> {

    @AssignedName("block.peculiar_clump.resonance")
    public static final SoundEvent BLOCK_PECULIAR_CLUMP_RESONANCE = SoundEvent.of(Affinity.id("block.peculiar_clump.resonance"));

    @AssignedName("block.ritual_socle.activate")
    public static final SoundEvent BLOCK_RITUAL_SOCLE_ACTIVATE = SoundEvent.of(Affinity.id("block.ritual_socle.activate"));

    @AssignedName("block.aberrant_calling_core.ritual_success")
    public static final SoundEvent BLOCK_ABERRANT_CALLING_CORE_RITUAL_SUCCESS = SoundEvent.of(Affinity.id("block.aberrant_calling_core.ritual_success"));

    @AssignedName("item.artifact_blade.smash")
    public static final SoundEvent ITEM_ARTIFACT_BLADE_SMASH = SoundEvent.of(Affinity.id("item.artifact_blade.smash"));

    @Override
    public Registry<SoundEvent> getRegistry() {
        return Registries.SOUND_EVENT;
    }

    @Override
    public Class<SoundEvent> getTargetFieldType() {
        return SoundEvent.class;
    }
}
