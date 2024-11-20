package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.affinity.object.AffinityEnchantmentEffectComponents;
import io.wispforest.affinity.object.AffinityEnchantments;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import static io.wispforest.affinity.item.VillagerArmsItem.VILLAGER_DATA;
import static io.wispforest.affinity.object.AffinityBlocks.*;
import static io.wispforest.affinity.object.AffinityItems.*;

public class AffinityItemGroup {

    private static OwoItemGroup GROUP;

    public static void register() {
        GROUP = OwoItemGroup.builder(Affinity.id("affinity"), () -> Icon.of(INERT_WISP_MATTER)).initializer(group -> {
            //noinspection Convert2MethodRef
            initializeGroup(group);
        }).build();
    }

    public static OwoItemGroup group() {
        return Objects.requireNonNull(GROUP, "Affinity item group not initialized");
    }

    @Environment(EnvType.CLIENT)
    private static void initializeGroup(OwoItemGroup group) {
        group.addCustomTab(Icon.of(EMERALD_WAND_OF_IRIDESCENCE), "main", (context, entries) -> {
            entries.add(WISPEN_TESTAMENT);
            entries.add(EMERALD_WAND_OF_IRIDESCENCE);
            entries.add(WAND_OF_INQUIRY);
            entries.add(ARCANE_TREETAP);
            entries.add(ASSEMBLY_AUGMENT);
            entries.add(RUDIMENTARY_RITUAL_SOCLE);
            entries.add(REFINED_RITUAL_SOCLE);
            entries.add(SOPHISTICATED_RITUAL_SOCLE);
            entries.add(ASP_RITE_CORE);
            entries.add(SPIRIT_INTEGRATION_APPARATUS);
            entries.add(FIELD_COHERENCE_MODULATOR);
            entries.add(BLANK_RITUAL_SOCLE);
            entries.add(STONE_SOCLE_ORNAMENT);
            entries.add(PRISMARINE_SOCLE_ORNAMENT);
            entries.add(PURPUR_SOCLE_ORNAMENT);
            entries.add(RITUAL_SOCLE_COMPOSER);
            entries.add(UNFIRED_CLAY_CUP);
            entries.add(CLAY_CUP);
            entries.add(AFFINITEA);
            entries.add(MILK_CUP);
            entries.add(INERT_WISP_MATTER);
            entries.add(WISE_WISP_MATTER);
            entries.add(VICIOUS_WISP_MATTER);
            entries.add(INERT_WISP_MIST);
            entries.add(WISE_WISP_MIST);
            entries.add(VICIOUS_WISP_MIST);
            entries.add(AETHUM_MAP_PROTOTYPE);
            entries.add(AETHUM_OVERCHARGER);
            entries.add(BUDDING_EXPERIENCE_CRYSTAL);
            entries.add(CRYSTALLIZED_EXPERIENCE);
            entries.add(GEOLOGICAL_RESONATOR);
            entries.add(BARE_RESPLENDENT_GEM);
            entries.add(ANTHRACITE_POWDER);
            entries.add(SCINTILLANT_ANTHRACITE_BLEND);
            entries.add(RANTHRACITE_DUST);
            entries.add(Items.AMETHYST_SHARD);
            entries.add(MILDLY_ATTUNED_AMETHYST_SHARD);
            entries.add(FAIRLY_ATTUNED_AMETHYST_SHARD);
            entries.add(GREATLY_ATTUNED_AMETHYST_SHARD);
            entries.add(SCULK_RESONANT_ETHEREAL_AMETHYST_SHARD);
            entries.add(VOID_RESONANT_ETHEREAL_AMETHYST_SHARD);
            entries.add(ARCANE_FADE_BUCKET);
            entries.add(AETHUM_FLUX_BOTTLE);
            if (Affinity.config().unfinishedFeatures()) entries.add(PITCHER_ELIXIR_BOTTLE);
            entries.add(EMERALD_BLOCK);
            entries.add(EMERALD_INGOT);
            entries.add(EMERALD_NUGGET);
            entries.add(UNCANNY_ROD);
            entries.add(STAFF_PROTOTYPE);
            entries.add(CRYSTALLINE_WISP_MATTER_COMPOSITE);
            entries.add(RESONANCE_CRYSTAL);
            entries.add(DRAGON_DROP);
            entries.add(SYNTHETIC_DRAGON_HEART);
            entries.add(ARCHETYPAL_IRON_RING);
            entries.add(BLACKSTONE_PRISM);
            entries.add(INFUSED_STONE);
        }, true);

        group.addCustomTab(Icon.of(AffinityBlocks.COPPER_PLATED_AETHUM_FLUX_NODE), "machinery", (context, entries) -> {
            entries.add(STONE_BANDED_AETHUM_FLUX_NODE);
            entries.add(COPPER_PLATED_AETHUM_FLUX_NODE);
            entries.add(ETHEREAL_AETHUM_FLUX_INJECTOR);
            entries.add(ETHEREAL_AETHUM_FLUX_NODE);
            entries.add(AETHUM_FLUX_CACHE);
            entries.add(CREATIVE_AETHUM_FLUX_CACHE);
            entries.add(MATTER_HARVESTING_HEARTH);
            entries.add(ARBOREAL_ANNIHILATION_APPARATUS);
            entries.add(CROP_REAPER);
            entries.add(GRAVITON_TRANSDUCER);
            entries.add(STAFF_PEDESTAL);
            entries.add(AFFINE_INFUSER);
            entries.add(BREWING_CAULDRON);
            entries.add(SUNSHINE_MONOLITH);
            entries.add(AFFINE_CANDLE);
            entries.add(VOID_BEACON);
            entries.add(MANGROVE_BASKET);
            entries.add(HOLOGRAPHIC_STEREOPTICON);
            entries.add(OUIJA_BOARD);
            entries.add(AETHUM_PROBE);
            entries.add(ITEM_TRANSFER_NODE);
            entries.add(PHANTOM_BUNDLE);
            entries.add(WORLD_PIN);
            entries.add(LOCAL_DISPLACEMENT_GATEWAY);
            if (Affinity.config().unfinishedFeatures()) entries.add(VILLAGER_ARMATURE);
            entries.add(Items.AMETHYST_SHARD);
            entries.add(MILDLY_ATTUNED_AMETHYST_SHARD);
            entries.add(FAIRLY_ATTUNED_AMETHYST_SHARD);
            entries.add(GREATLY_ATTUNED_AMETHYST_SHARD);
            entries.add(SCULK_RESONANT_ETHEREAL_AMETHYST_SHARD);
            entries.add(VOID_RESONANT_ETHEREAL_AMETHYST_SHARD);
        }, false);

        group.addCustomTab(Icon.of(RESOUNDING_CHIME), "equipment", (context, entries) -> {
            entries.add(COLLECTION_STAFF);
            entries.add(NIMBLE_STAFF);
            entries.add(TIME_STAFF);
            entries.add(KINESIS_STAFF);
            entries.add(ASTROKINESIS_STAFF);
            entries.add(CULTIVATION_STAFF);
            entries.add(SALVO_STAFF);
            entries.add(SWIVEL_STAFF);
            entries.add(FEATHERWEIGHT_RING);
            entries.add(EVADE_RING);
            entries.add(LAVALIERE_OF_SAFE_KEEPING);
            entries.add(ASSASSINS_QUIVER);
            entries.add(AETHUM_FIRE_EXTINGUISHER);
            entries.add(FORGOTTEN_ARTIFACT_BLADE);
            entries.add(STABILIZED_ARTIFACT_BLADE);
            entries.add(STRENGTHENED_ARTIFACT_BLADE);
            entries.add(SUPERIOR_ARTIFACT_BLADE);
            entries.add(ASTRAL_ARTIFACT_BLADE);
            entries.add(RESOUNDING_CHIME);
            entries.add(AZALEA_BOW);
            entries.add(EMERALD_HELMET);
            entries.add(EMERALD_CHESTPLATE);
            entries.add(EMERALD_LEGGINGS);
            entries.add(EMERALD_BOOTS);

            context.lookup().getOptionalWrapper(RegistryKeys.ENCHANTMENT).ifPresent(wrapper -> {
                entries.add(ResplendentGemItem.make(AffinityEnchantments.BERSERKER, wrapper));
                entries.add(ResplendentGemItem.make(AffinityEnchantments.GRAVECALLER, wrapper));
                entries.add(ResplendentGemItem.make(AffinityEnchantments.BASTION, wrapper));

                wrapper.streamEntries()
                    .filter(entry -> entry.registryKey().getValue().getNamespace().equals(Affinity.MOD_ID))
                    .filter(enchantment -> !enchantment.value().effects().contains(AffinityEnchantmentEffectComponents.ABSOLUTE_NAME_HUE))
                    .map(enchantment -> new EnchantmentLevelEntry(enchantment, enchantment.value().getMaxLevel()))
                    .map(EnchantedBookItem::forEnchantment)
                    .forEach(entries::add);
            });

            context.lookup().getOptionalWrapper(RegistryKeys.POTION).ifPresent(wrapper -> {
                addPotions(entries, wrapper, Items.POTION);
                addPotions(entries, wrapper, Items.SPLASH_POTION);
                addPotions(entries, wrapper, Items.LINGERING_POTION);
                addPotions(entries, wrapper, Items.TIPPED_ARROW);
            });
        }, false);

        group.addCustomTab(Icon.of(isChyz() ? THE_SKY : AZALEA_LOG), "nature", (context, entries) -> {
            entries.add(AZALEA_LOG);
            entries.add(AZALEA_WOOD);
            entries.add(STRIPPED_AZALEA_LOG);
            entries.add(STRIPPED_AZALEA_WOOD);
            entries.add(AZALEA_PLANKS);
            entries.add(AZALEA_STAIRS);
            entries.add(AZALEA_SLAB);
            entries.add(AZALEA_FENCE);
            entries.add(AZALEA_FENCE_GATE);
            entries.add(AZALEA_DOOR);
            entries.add(AZALEA_TRAPDOOR);
            entries.add(AZALEA_PRESSURE_PLATE);
            entries.add(AZALEA_BUTTON);
            entries.add(AffinityItems.AZALEA_SIGN);
            entries.add(AffinityItems.AZALEA_HANGING_SIGN);
            entries.add(AZALEA_BOAT);
            entries.add(AZALEA_CHEST_BOAT);
            entries.add(AZALEA_CHEST);
            entries.add(BUDDING_AZALEA_LEAVES);
            entries.add(Blocks.FLOWERING_AZALEA_LEAVES);
            entries.add(UNFLOWERING_AZALEA_LEAVES);
            entries.add(AZALEA_FLOWERS);
            entries.add(SOUP_OF_BEE);
            entries.add(SATIATING_POTION);
            entries.add(THE_SKY);
            entries.add(PECULIAR_CLUMP);
            entries.add(INERT_WISP_SPAWN_EGG);
            entries.add(WISE_WISP_SPAWN_EGG);
            entries.add(VICIOUS_WISP_SPAWN_EGG);

            if (Affinity.config().unfinishedFeatures()) {
                var armStacks = new ArrayList<ItemStack>();
                for (var villagerProfession : Registries.VILLAGER_PROFESSION) {
                    var stack = VILLAGER_ARMS.getDefaultStack();
                    stack.set(VILLAGER_DATA, new VillagerArmsItem.ArmsData(VillagerType.PLAINS, villagerProfession, 5));
                    armStacks.add(stack);
                }
                entries.addAll(armStacks, ItemGroup.StackVisibility.PARENT_TAB_ONLY);

                armStacks.clear();
                for (var villagerType : Registries.VILLAGER_TYPE) {
                    for (var villagerProfession : Registries.VILLAGER_PROFESSION) {
                        for (int i = 1; i < ((villagerProfession.equals(VillagerProfession.NITWIT) || villagerProfession.equals(VillagerProfession.NONE)) ? 2 : 6); i++) {
                            var stack = VILLAGER_ARMS.getDefaultStack();
                            stack.set(VILLAGER_DATA, new VillagerArmsItem.ArmsData(villagerType, villagerProfession, i));
                            armStacks.add(stack);
                        }
                    }
                }
                entries.addAll(armStacks, ItemGroup.StackVisibility.SEARCH_TAB_ONLY);
            }
        }, false);

        group.addButton(ItemGroupButton.github(group, "https://github.com/wisp-forest/affinity"));
        group.addButton(ItemGroupButton.modrinth(group, "https://modrinth.com/mod/affinity"));
        group.addButton(ItemGroupButton.curseforge(group, "https://curseforge.com/minecraft/mc-mods/affinity"));
    }

    private static void addPotions(ItemGroup.Entries entries, RegistryWrapper<Potion> registryWrapper, Item containerItem) {
        registryWrapper.streamEntries()
            .filter(entry -> entry.registryKey().getValue().getNamespace().equals(Affinity.MOD_ID))
            .filter(entry -> !entry.matches(Potions.WATER))
            .map(entry -> PotionContentsComponent.createStack(containerItem, entry))
            .forEach(entries::add);
    }

    private static boolean isChyz() {
        return Calendar.getInstance().get(Calendar.MONTH) == Calendar.MAY && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 16;
    }
}
