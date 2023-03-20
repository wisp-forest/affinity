package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.world.poi.PointOfInterestType;

public class AffinityPoiTypes {

    public static final PointOfInterestType RITUAL_CORE = PointOfInterestHelper.register(Affinity.id("ritual_core"),
            0, 1, AffinityBlocks.ASP_RITE_CORE, AffinityBlocks.SPIRIT_INTEGRATION_APPARATUS);

    public static final PointOfInterestType RITUAL_SOCLE = PointOfInterestHelper.register(Affinity.id("ritual_socle"), 0, 1,
            AffinityBlocks.RUDIMENTARY_RITUAL_SOCLE, AffinityBlocks.REFINED_RITUAL_SOCLE, AffinityBlocks.SOPHISTICATED_RITUAL_SOCLE);

    public static final PointOfInterestType AFFINE_CANDLE = PointOfInterestHelper.register(Affinity.id("affine_candle"), 0, 1,
            AffinityBlocks.AFFINE_CANDLE);

    public static final PointOfInterestType ARCANE_TREETAP = PointOfInterestHelper.register(Affinity.id("arcane_treetap"), 0, 1,
            AffinityBlocks.ARCANE_TREETAP);

    public static void initialize() {}
}
