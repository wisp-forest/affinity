package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.item.*;
import io.wispforest.affinity.object.attunedshards.AttunedShardTiers;
import io.wispforest.affinity.object.rituals.AffinityRitualSocleTypes;
import io.wispforest.affinity.object.wisps.AffinityWispTypes;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import io.wispforest.owo.registration.reflect.ItemRegistryContainer;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.BoatItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.SignItem;

@SuppressWarnings("unused")
public class AffinityItems implements ItemRegistryContainer {

    public static final Item AETHUM_FLUX_BOTTLE = new AethumFluxBottleItem();
    public static final Item RESPLENDENT_GEM = new ResplendentGemItem();

    public static final Item MILDLY_ATTUNED_AMETHYST_SHARD = new AttunedShardItem(AttunedShardTiers.MILDLY_ATTUNED);
    public static final Item FAIRLY_ATTUNED_AMETHYST_SHARD = new AttunedShardItem(AttunedShardTiers.FAIRLY_ATTUNED);
    public static final Item GREATLY_ATTUNED_AMETHYST_SHARD = new AttunedShardItem(AttunedShardTiers.GREATLY_ATTUNED);

    public static final Item EMERALD_WAND_OF_IRIDESCENCE = new IridescenceWandItem();
    public static final Item SAPPHIRE_WAND_OF_IRIDESCENCE = new IridescenceWandItem();
    public static final Item WAND_OF_INQUIRY = new WandOfInquiryItem();
    public static final Item GEOLOGICAL_RESONATOR = new GeologicalResonatorItem();
    public static final Item COLLECTION_STAFF = new CollectionStaffItem();
    public static final Item NIMBLE_STAFF = new NimbleStaffItem();
    public static final Item TIME_STAFF = new TimeStaffItem();
    public static final Item KINESIS_STAFF = new KinesisStaffItem();

    public static final Item AZALEA_FLOWERS = new Item(settings(AffinityItemGroup.NATURE).food(new FoodComponent.Builder().hunger(2).saturationModifier(.5f)
            .statusEffect(new StatusEffectInstance(AffinityStatusEffects.DRIPPING, 1200, 0, false, false, true), 1).build()));
    public static final Item SOUP_OF_BEE = new SoupOfBeeItem();

    public static final Item INERT_WISP_MATTER = new WispMatterItem(AffinityWispTypes.INERT);
    public static final Item WISE_WISP_MATTER = new WispMatterItem(AffinityWispTypes.WISE);
    public static final Item VICIOUS_WISP_MATTER = new WispMatterItem(AffinityWispTypes.VICIOUS);

    public static final Item STONE_SOCLE_ORNAMENT = new SocleOrnamentItem(AffinityRitualSocleTypes.RUDIMENTARY);
    public static final Item PRISMARINE_SOCLE_ORNAMENT = new SocleOrnamentItem(AffinityRitualSocleTypes.REFINED);
    public static final Item PURPUR_SOCLE_ORNAMENT = new SocleOrnamentItem(AffinityRitualSocleTypes.SOPHISTICATED);

    public static final Item ANTHRACITE_POWDER = new Item(settings(AffinityItemGroup.MAIN));
    public static final Item AETHUM_MAP_PROTOTYPE = new AethumMapPrototypeItem();
    public static final Item REALIZED_AETHUM_MAP = new RealizedAethumMapItem();

    public static final Item AZALEA_SIGN = new SignItem(settings(AffinityItemGroup.NATURE).maxCount(16), AffinityBlocks.AZALEA_SIGN, AffinityBlocks.AZALEA_WALL_SIGN);
    public static final Item AZALEA_BOAT = new BoatItem(false, AffinityBlocks.AZALEA_BOAT_TYPE, settings(AffinityItemGroup.NATURE).maxCount(1));
    public static final Item AZALEA_CHEST_BOAT = new BoatItem(true, AffinityBlocks.AZALEA_BOAT_TYPE, settings(AffinityItemGroup.NATURE).maxCount(1));

    public static final Item AFFINITEA = new AffiniteaItem();
    public static final Item DRAGON_DROP = new Item(settings(AffinityItemGroup.MAIN));

    public static final Item FORGOTTEN_ARTIFACT_BLADE = new ArtifactBladeItem(ArtifactBladeItem.Tier.FORGOTTEN);
    public static final Item STABILIZED_ARTIFACT_BLADE = new ArtifactBladeItem(ArtifactBladeItem.Tier.STABILIZED);
    public static final Item STRENGTHENED_ARTIFACT_BLADE = new ArtifactBladeItem(ArtifactBladeItem.Tier.STRENGTHENED);
    public static final Item SUPERIOR_ARTIFACT_BLADE = new ArtifactBladeItem(ArtifactBladeItem.Tier.SUPERIOR);
    public static final Item ASTRAL_ARTIFACT_BLADE = new ArtifactBladeItem(ArtifactBladeItem.Tier.ASTRAL);

    public static OwoItemSettings settings(int tab) {
        return new OwoItemSettings().tab(tab).group(Affinity.AFFINITY_GROUP);
    }
}
