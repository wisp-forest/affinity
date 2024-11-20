package io.wispforest.affinity.object;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.item.*;
import io.wispforest.affinity.object.attunedshards.AttunedShardTiers;
import io.wispforest.affinity.object.rituals.AffinityRitualSocleTypes;
import io.wispforest.affinity.object.wisps.AffinityWispTypes;
import io.wispforest.lavender.book.LavenderBookItem;
import io.wispforest.owo.registration.annotations.IterationIgnored;
import io.wispforest.owo.registration.reflect.ItemRegistryContainer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class AffinityItems implements ItemRegistryContainer {

    @IterationIgnored
    public static final Item WISPEN_TESTAMENT = LavenderBookItem.registerForBook(Affinity.id("wispen_testament"), settings().maxCount(1));

    public static final Item AETHUM_FLUX_BOTTLE = new AethumFluxBottleItem();
    public static final Item PITCHER_ELIXIR_BOTTLE = new Item(settings().maxCount(16).rarity(Rarity.UNCOMMON));
    public static final Item BARE_RESPLENDENT_GEM = new Item(settings().maxCount(4).rarity(Rarity.UNCOMMON));
    public static final Item RESPLENDENT_GEM = new ResplendentGemItem();

    public static final Item MILDLY_ATTUNED_AMETHYST_SHARD = new AttunedShardItem(settings(), AttunedShardTiers.MILDLY_ATTUNED);
    public static final Item FAIRLY_ATTUNED_AMETHYST_SHARD = new AttunedShardItem(settings().rarity(Rarity.UNCOMMON), AttunedShardTiers.FAIRLY_ATTUNED);
    public static final Item GREATLY_ATTUNED_AMETHYST_SHARD = new AttunedShardItem(settings().rarity(Rarity.RARE), AttunedShardTiers.GREATLY_ATTUNED);
    public static final Item SCULK_RESONANT_ETHEREAL_AMETHYST_SHARD = new Item(settings());
    public static final Item VOID_RESONANT_ETHEREAL_AMETHYST_SHARD = new Item(settings().rarity(Rarity.EPIC));

    public static final Item EMERALD_WAND_OF_IRIDESCENCE = new IridescenceWandItem();
    public static final Item SAPPHIRE_WAND_OF_IRIDESCENCE = new IridescenceWandItem();
    public static final Item WAND_OF_INQUIRY = new WandOfInquiryItem();
    public static final Item GEOLOGICAL_RESONATOR = new GeologicalResonatorItem();
    public static final Item COLLECTION_STAFF = new CollectionStaffItem();
    public static final Item NIMBLE_STAFF = new NimbleStaffItem();
    public static final Item TIME_STAFF = new TimeStaffItem();
    public static final Item KINESIS_STAFF = new KinesisStaffItem();
    public static final Item ASTROKINESIS_STAFF = new AstrokinesisStaffItem();
    public static final Item CULTIVATION_STAFF = new CultivationStaffItem();
    public static final Item SALVO_STAFF = new SalvoStaffItem();
    public static final Item SWIVEL_STAFF = new SwivelStaffItem();

    public static final Item AZALEA_FLOWERS = new Item(settings().food(new FoodComponent.Builder().nutrition(2).saturationModifier(.5f)
            .statusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(AffinityStatusEffects.DRIPPING), 1200, 0, false, false, true), 1).build()));
    public static final Item SOUP_OF_BEE = new SoupOfBeeItem();

    public static final Item INERT_WISP_MATTER = new WispMatterItem(AffinityWispTypes.INERT);
    public static final Item INERT_WISP_MIST = new WispMistItem(AffinityWispTypes.INERT);
    public static final Item WISE_WISP_MATTER = new WispMatterItem(AffinityWispTypes.WISE);
    public static final Item WISE_WISP_MIST = new WispMistItem(AffinityWispTypes.WISE);
    public static final Item VICIOUS_WISP_MATTER = new WispMatterItem(AffinityWispTypes.VICIOUS);
    public static final Item VICIOUS_WISP_MIST = new WispMistItem(AffinityWispTypes.VICIOUS);

    public static final Item STONE_SOCLE_ORNAMENT = new SocleOrnamentItem(AffinityRitualSocleTypes.RUDIMENTARY);
    public static final Item PRISMARINE_SOCLE_ORNAMENT = new SocleOrnamentItem(AffinityRitualSocleTypes.REFINED);
    public static final Item PURPUR_SOCLE_ORNAMENT = new SocleOrnamentItem(AffinityRitualSocleTypes.SOPHISTICATED);

    public static final Item ANTHRACITE_POWDER = new AnthracitePowderItem();
    public static final Item CARBON_COPY = new CarbonCopyItem();
    public static final Item SCINTILLANT_ANTHRACITE_BLEND = new Item(settings());
    public static final Item RANTHRACITE_DUST = new AliasedBlockItem(AffinityBlocks.RANTHRACITE_WIRE, settings());
    public static final Item CRYSTALLINE_WISP_MATTER_COMPOSITE = new GlintItem(settings().rarity(Rarity.UNCOMMON));
    public static final Item BLACKSTONE_PRISM = new Item(settings().maxCount(8));
    public static final Item AETHUM_MAP_PROTOTYPE = new AethumMapPrototypeItem();
    public static final Item REALIZED_AETHUM_MAP = new RealizedAethumMapItem();
    public static final Item UNCANNY_ROD = new Item(settings().maxCount(16));
    public static final Item STAFF_PROTOTYPE = new Item(settings().maxCount(7));
    public static final Item VILLAGER_ARMS = new VillagerArmsItem(settings().maxCount(3));
    public static final Item PHANTOM_BUNDLE = new PhantomBundleItem(settings().maxCount(1));

    public static final Item AZALEA_SIGN = new SignItem(settings().maxCount(16), AffinityBlocks.AZALEA_SIGN, AffinityBlocks.AZALEA_WALL_SIGN);
    public static final Item AZALEA_HANGING_SIGN = new HangingSignItem(AffinityBlocks.AZALEA_HANGING_SIGN, AffinityBlocks.AZALEA_WALL_HANGING_SIGN, settings().maxCount(16));
    public static final Item AZALEA_BOAT = new BoatItem(false, AffinityBlocks.AZALEA_BOAT_TYPE, settings().maxCount(1));
    public static final Item AZALEA_CHEST_BOAT = new BoatItem(true, AffinityBlocks.AZALEA_BOAT_TYPE, settings().maxCount(1));

    public static final Item UNFIRED_CLAY_CUP = new Item(settings());
    public static final Item CLAY_CUP = new Item(settings());
    public static final Item AFFINITEA = new AffiniteaItem();
    public static final Item MILK_CUP = new MilkCupItem();
    public static final Item SATIATING_POTION = new SatiatingPotionItem();
    public static final Item DRAGON_DROP = new GlintItem(settings().rarity(Rarity.UNCOMMON));
    public static final Item RESONANCE_CRYSTAL = new GlintItem(settings().rarity(Rarity.UNCOMMON));
    public static final Item SYNTHETIC_DRAGON_HEART = new GlintItem(settings().rarity(Rarity.RARE));
    public static final Item AETHUM_OVERCHARGER = new GlintItem(settings().rarity(Rarity.RARE).maxCount(1));
    public static final Item ARCHETYPAL_IRON_RING = new Item(settings());
    public static final Item FEATHERWEIGHT_RING = new FeatherweightRingItem();
    public static final Item EVADE_RING = new EvadeRingItem();
    public static final Item LAVALIERE_OF_SAFE_KEEPING = new LavaliereOfSafeKeepingItem();
    public static final Item ASSASSINS_QUIVER = new AssassinsQuiverItem();
    public static final Item AETHUM_FIRE_EXTINGUISHER = new AethumFireExtinguisherItem();
    public static final Item BUDDING_EXPERIENCE_CRYSTAL = new BuddingExperienceCrystalItem();
    public static final Item CRYSTALLIZED_EXPERIENCE = new CrystallizedExperienceItem();

    public static final Item ARCANE_FADE_BUCKET = new BucketItem(AffinityBlocks.Fluids.ARCANE_FADE, settings().maxCount(1).recipeRemainder(Items.BUCKET));

    public static final Item FORGOTTEN_ARTIFACT_BLADE = new ArtifactBladeItem(ArtifactBladeItem.Tier.FORGOTTEN);
    public static final Item STABILIZED_ARTIFACT_BLADE = new ArtifactBladeItem(ArtifactBladeItem.Tier.STABILIZED);
    public static final Item STRENGTHENED_ARTIFACT_BLADE = new ArtifactBladeItem(ArtifactBladeItem.Tier.STRENGTHENED);
    public static final Item SUPERIOR_ARTIFACT_BLADE = new ArtifactBladeItem(ArtifactBladeItem.Tier.SUPERIOR);
    public static final Item ASTRAL_ARTIFACT_BLADE = new ArtifactBladeItem(ArtifactBladeItem.Tier.ASTRAL);
    public static final Item RESOUNDING_CHIME = new ResoundingChimeItem();
    public static final Item AZALEA_BOW = new AzaleaBowItem();

    public static final Item EMERALD_HELMET = new EmeraldArmorItem(ArmorItem.Type.HELMET);
    public static final Item EMERALD_CHESTPLATE = new EmeraldArmorItem(ArmorItem.Type.CHESTPLATE);
    public static final Item EMERALD_LEGGINGS = new EmeraldArmorItem(ArmorItem.Type.LEGGINGS);
    public static final Item EMERALD_BOOTS = new EmeraldArmorItem(ArmorItem.Type.BOOTS);

    public static final Item EMERALD_INGOT = new Item(settings().rarity(Rarity.UNCOMMON));
    public static final Item EMERALD_NUGGET = new Item(settings().rarity(Rarity.UNCOMMON));

    public static final Item INERT_WISP_SPAWN_EGG = new WispSpawnEggItem(AffinityEntities.INERT_WISP, AffinityWispTypes.INERT);
    public static final Item WISE_WISP_SPAWN_EGG = new WispSpawnEggItem(AffinityEntities.WISE_WISP, AffinityWispTypes.WISE);
    public static final Item VICIOUS_WISP_SPAWN_EGG = new WispSpawnEggItem(AffinityEntities.VICIOUS_WISP, AffinityWispTypes.VICIOUS);

    public static Item.Settings settings() {
        return new Item.Settings();
    }

    public static ItemStack makePotionOfInfiniteProwess() {
        var potion = new ItemStack(Items.POTION);
        var component = new PotionContentsComponent(
                Optional.of(Potions.LONG_STRENGTH),
                Optional.empty(),
                Stream.generate(() -> new StatusEffectInstance(StatusEffects.STRENGTH, 9600)).limit(24).toList()
        );

        potion.set(DataComponentTypes.ITEM_NAME, Text.translatable("item.affinity.potion_of_infinite_prowess"));
        potion.set(DataComponentTypes.POTION_CONTENTS, component);
        return potion;
    }

    private static class GlintItem extends Item {

        public GlintItem(Settings settings) {
            super(settings);
        }

        @Override
        public boolean hasGlint(ItemStack stack) {
            return true;
        }
    }
}
