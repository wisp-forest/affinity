package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.registration.reflect.SimpleFieldProcessingSubject;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

import java.lang.reflect.Field;

public class AffinitySoundEvents implements SimpleFieldProcessingSubject<SoundEvent> {

    public static final SoundEvent BLOCK_SPIRIT_INTEGRATION_APPARATUS_RITUAL_START = SoundEvent.of(Affinity.id("block.spirit_integration_apparatus.ritual_start"));
    public static final SoundEvent BLOCK_ASP_RITE_CORE_CRAFT = SoundEvent.of(Affinity.id("block.asp_rite_core.craft"));
    public static final SoundEvent BLOCK_ASP_RITE_CORE_ACTIVE = SoundEvent.of(Affinity.id("block.asp_rite_core.active"));
    public static final SoundEvent BLOCK_ASP_RITE_CORE_RITUAL_FAIL = SoundEvent.of(Affinity.id("block.asp_rite_core.ritual_fail"));
    public static final SoundEvent BLOCK_GRAVITON_TRANSDUCER_TRANSDUCE = SoundEvent.of(Affinity.id("block.graviton_transducer.transduce"));
    public static final SoundEvent ITEM_ARTIFACT_BLADE_SMASH = SoundEvent.of(Affinity.id("item.artifact_blade.smash"));
    public static final SoundEvent ITEM_IRIDESCENCE_WAND_BIND = SoundEvent.of(Affinity.id("item.iridescence_wand.bind"));
    public static final SoundEvent ITEM_SATIATING_POTION_START_DRINKING = SoundEvent.of(Affinity.id("item.satiating_potion.start_drinking"));
    public static final SoundEvent ITEM_NIMBLE_STAFF_FLING = SoundEvent.of(Affinity.id("item.nimble_staff.fling"));
    public static final SoundEvent ITEM_SALVO_STAFF_HIT = SoundEvent.of(Affinity.id("item.salvo_staff.hit"));
    public static final SoundEvent ITEM_EVADE_RING_EVADE = SoundEvent.of(Affinity.id("item.evade_ring.evade"));
    public static final SoundEvent ITEM_SATIATING_POTION_DRINK = SoundEvent.of(Affinity.id("item.satiating_potion.drink"));
    public static final SoundEvent EFFECT_FLIGHT_DURATION_WARNING = SoundEvent.of(Affinity.id("effect.flight.duration_warning"));
    public static final SoundEvent EFFECT_FLIGHT_INTERRUPTED = SoundEvent.of(Affinity.id("effect.flight.interrupted"));
    public static final SoundEvent BLOCK_BREWING_CAULDRON_BREW = SoundEvent.of(Affinity.id("block.brewing_cauldron.brew"));
    public static final SoundEvent ENTITY_VILLAGER_STRIP_ARMS = SoundEvent.of(Affinity.id("entity.villager.strip_arms"));
    public static final SoundEvent ITEM_SWIVEL_STAFF_SELECT_PROPERTY = SoundEvent.of(Affinity.id("item.swivel_staff.select_property"));
    public static final SoundEvent ITEM_SWIVEL_STAFF_SWIVEL = SoundEvent.of(Affinity.id("item.swivel_staff.swivel"));
    public static final SoundEvent BLOCK_BREWING_CAULDRON_PICK_UP_ITEM = SoundEvent.of(Affinity.id("block.brewing_cauldron.pick_up_item"));
    public static final SoundEvent BLOCK_VOID_BEACON_ACTIVATE = SoundEvent.of(Affinity.id("block.void_beacon.activate"));
    public static final SoundEvent ITEM_AETHUM_OVERCHARGER_ACTIVATE = SoundEvent.of(Affinity.id("item.aethum_overcharger.activate"));
    public static final SoundEvent ITEM_GEOLOGICAL_RESONATOR_RESONATE = SoundEvent.of(Affinity.id("item.geological_resonator.resonate"));
    public static final SoundEvent ITEM_GEOLOGICAL_RESONATOR_PLONK = SoundEvent.of(Affinity.id("item.geological_resonator.plonk"));
    public static final SoundEvent BLOCK_GRAVITON_TRANSDUCER_SHARD_BREAKS = SoundEvent.of(Affinity.id("block.graviton_transducer.shard_breaks"));
    public static final SoundEvent ITEM_COLLECTION_STAFF_MARK_ITEMS = SoundEvent.of(Affinity.id("item.collection_staff.mark_items"));
    public static final SoundEvent ITEM_COLLECTION_STAFF_TELEPORT_ITEMS = SoundEvent.of(Affinity.id("item.collection_staff.teleport_items"));
    public static final SoundEvent ITEM_CULTIVATION_STAFF_CULTIVATE = SoundEvent.of(Affinity.id("item.cultivation_staff.cultivate"));
    public static final SoundEvent BLOCK_SPIRIT_INTEGRATION_APPARATUS_DROP_ITEM = SoundEvent.of(Affinity.id("block.spirit_integration_apparatus.drop_item"));
    public static final SoundEvent BLOCK_SPIRIT_INTEGRATION_APPARATUS_RITUAL_FAIL = SoundEvent.of(Affinity.id("block.spirit_integration_apparatus.ritual_fail"));
    public static final SoundEvent BLOCK_SONIC_SYPHON_SHRIEK = SoundEvent.of(Affinity.id("block.sonic_syphon.shriek"));
    public static final SoundEvent ITEM_ECHO_SHARD_BIND = SoundEvent.of(Affinity.id("item.echo_shard.bind"));
    public static final SoundEvent ITEM_RESOUNDING_CHIME_DING = SoundEvent.of(Affinity.id("item.resounding_chime.ding"));
    public static final SoundEvent ITEM_EMERALD_ARMOR_EQUIP = SoundEvent.of(Affinity.id("item.emerald_armor.equip"));
    public static final SoundEvent ITEM_SALVO_STAFF_FIRE_MISSILE = SoundEvent.of(Affinity.id("item.salvo_staff.fire_missile"));
    public static final SoundEvent ITEM_STAFF_SELECT = SoundEvent.of(Affinity.id("item.staff.select"));
    public static final SoundEvent FLUID_ARCANE_FADE_BLEACH = SoundEvent.of(Affinity.id("fluid.arcane_fade.bleach"));
    public static final SoundEvent FLUID_ARCANE_FADE_CRAFT = SoundEvent.of(Affinity.id("fluid.arcane_fade.craft"));

    @Override
    public void processField(SoundEvent value, String identifier, Field field) {
        Registry.register(Registries.SOUND_EVENT, value.getId(), value);
    }

    @Override
    public Class<SoundEvent> getTargetFieldType() {
        return SoundEvent.class;
    }
}
