package io.wispforest.affinity.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;
import net.minecraft.item.Item;

import java.util.Optional;

import static io.wispforest.affinity.object.AffinityItems.*;

public class AffinityItemModelProvider extends FabricModelProvider {

    public AffinityItemModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
        forAll(generator, Models.GENERATED,
                AZALEA_FLOWERS, AETHUM_FLUX_BOTTLE, AZALEA_BOAT, AZALEA_CHEST_BOAT, MILDLY_ATTUNED_AMETHYST_SHARD, FAIRLY_ATTUNED_AMETHYST_SHARD,
                GREATLY_ATTUNED_AMETHYST_SHARD, STONE_SOCLE_ORNAMENT, PRISMARINE_SOCLE_ORNAMENT, PURPUR_SOCLE_ORNAMENT, SOUP_OF_BEE, AETHUM_MAP_PROTOTYPE,
                REALIZED_AETHUM_MAP, ANTHRACITE_POWDER, RESPLENDENT_GEM, INERT_WISP_MATTER, WISE_WISP_MATTER, VICIOUS_WISP_MATTER, DRAGON_DROP,
                SATIATING_POTION, ARCANE_FADE_BUCKET, AETHUM_OVERCHARGER, EMERALD_HELMET, EMERALD_CHESTPLATE, EMERALD_LEGGINGS, EMERALD_BOOTS, EMERALD_INGOT,
                EMERALD_NUGGET, FEATHERWEIGHT_RING, SCINTILLANT_ANTHRACITE_BLEND, UNFIRED_CLAY_CUP, CLAY_CUP, RESONANCE_CRYSTAL, SYNTHETIC_DRAGON_HEART,
                ARCHETYPAL_IRON_RING, WISPEN_TESTAMENT, LAVALIERE_OF_SAFE_KEEPING, CRYSTALLIZED_EXPERIENCE, BUDDING_EXPERIENCE_CRYSTAL,
                INERT_WISP_MIST, VICIOUS_WISP_MIST, WISE_WISP_MIST, ASSASSINS_QUIVER, CRYSTALLINE_WISP_MATTER_COMPOSITE, MILK_CUP,
                SCULK_RESONANT_ETHEREAL_AMETHYST_SHARD, EVADE_RING, BLACKSTONE_PRISM, PHANTOM_BUNDLE, PITCHER_ELIXIR_BOTTLE
        );

        forAll(generator, Models.HANDHELD,
                COLLECTION_STAFF, NIMBLE_STAFF, TIME_STAFF, KINESIS_STAFF, ASTROKINESIS_STAFF, CULTIVATION_STAFF, WAND_OF_INQUIRY, RESOUNDING_CHIME,
                SALVO_STAFF, GEOLOGICAL_RESONATOR, UNCANNY_ROD, STAFF_PROTOTYPE, SWIVEL_STAFF
        );

        forAll(generator, new Model(Optional.of(ModelIds.getMinecraftNamespacedItem("template_spawn_egg")), Optional.empty()),
                INERT_WISP_SPAWN_EGG, WISE_WISP_SPAWN_EGG, VICIOUS_WISP_SPAWN_EGG
        );
    }

    private void forAll(ItemModelGenerator generator, Model model, Item... items) {
        for (var item : items) generator.register(item, model);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {}

    @Override
    public String getName() {
        return "Item Models";
    }
}
