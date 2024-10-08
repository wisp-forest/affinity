package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import io.wispforest.owo.registration.annotations.AssignedName;
import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

// TODO: migrate to RegistryEntry
public class AffinityEntityAttributes implements AutoRegistryContainer<EntityAttribute> {

    @AssignedName("player.max_aethum")
    public static final EntityAttribute MAX_AETHUM = new ClampedEntityAttribute("attribute.name.player." + Affinity.MOD_ID + ".max_aethum", 0, 0, 1000);

    @AssignedName("player.natural_aethum_regen_speed")
    public static final EntityAttribute NATURAL_AETHUM_REGEN_SPEED = new ClampedEntityAttribute("attribute.name.player." + Affinity.MOD_ID + ".natural_aethum_regen_speed", 0, 0, 1000);

    @AssignedName("generic.damage_taken")
    public static final EntityAttribute DAMAGE_TAKEN = new ClampedEntityAttribute("attribute.name.generic." + Affinity.MOD_ID + ".damage_taken", 0, 0, 1000);

    @AssignedName("generic.knockback_susceptibility")
    public static final EntityAttribute KNOCKBACK_SUSCEPTIBILITY = new ClampedEntityAttribute("attribute.name.generic." + Affinity.MOD_ID + ".knockback_susceptibility", 0, 0, 1000);

    @AssignedName("generic.fall_resistance")
    public static final EntityAttribute FALL_RESISTANCE = new ClampedEntityAttribute("attribute.name.generic." + Affinity.MOD_ID + ".fall_resistance", 0, 0, 1000);

    @AssignedName("generic.extra_arrow_damage")
    public static final EntityAttribute EXTRA_ARROW_DAMAGE = new ClampedEntityAttribute("attribute.name.generic." + Affinity.MOD_ID + ".extra_arrow_damage", 0, 0, 1000);

    @Override
    public Registry<EntityAttribute> getRegistry() {
        return Registries.ATTRIBUTE;
    }

    @Override
    public Class<EntityAttribute> getTargetFieldType() {
        return EntityAttribute.class;
    }

    public static void initialize() { /* trigger static init */ }

    static {
        // Apparently I need to register all of these attributes way earlier than mod init so that PlayerEntityMixin can
        // get them.
        // TODO: ???
        AutoRegistryContainer.register(AffinityEntityAttributes.class, Affinity.MOD_ID, false);
    }
}
