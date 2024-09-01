package io.wispforest.affinity.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.component.AffinityComponents;
import io.wispforest.affinity.misc.DamageTypeKey;
import io.wispforest.affinity.misc.callback.ReplaceAttackDamageTextCallback;
import io.wispforest.affinity.misc.quack.AffinityEntityAddon;
import io.wispforest.affinity.object.AffinityEntityAttributes;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.endec.Endec;
import io.wispforest.owo.ops.TextOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Language;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class ArtifactBladeItem extends SwordItem {

    public static final DamageTypeKey DAMAGE_TYPE = new DamageTypeKey(Affinity.id("artifact_blade"));
    public static final AffinityEntityAddon.DataKey<Boolean> DID_CRIT = AffinityEntityAddon.DataKey.withDefaultConstant(false);

    private static final ComponentType<Long> ABILITY_START_TIME = Affinity.component("artifact_blade_ability_start_time", Endec.LONG);
    private static final EntityAttributeModifier DAMAGE_MODIFIER = new EntityAttributeModifier(Affinity.id("aethum_ability_damage_boost"), 2, EntityAttributeModifier.Operation.ADD_VALUE);

    public final Tier tier;

    public ArtifactBladeItem(Tier tier) {
        super(tier, AffinityItems.settings().maxCount(1).rarity(tier.data.rarity).trackUsageStat().attributeModifiers(SwordItem.createAttributeModifiers(tier, 0, tier.data.attackSpeed)));
        this.tier = tier;
    }


    @Override
    public void deriveStackComponents(ComponentMap source, ComponentChanges.Builder target) {
        if (!source.contains(ABILITY_START_TIME)) return;

        var attributes = source.getOrDefault(
            DataComponentTypes.ATTRIBUTE_MODIFIERS,
            AttributeModifiersComponent.DEFAULT
        ).with(
            EntityAttributes.GENERIC_ATTACK_DAMAGE,
            DAMAGE_MODIFIER, AttributeModifierSlot.MAINHAND
        );

        target.add(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var playerStack = user.getStackInHand(hand);
        if (playerStack.contains(ABILITY_START_TIME)) return TypedActionResult.pass(playerStack);

        var aethum = user.getComponent(AffinityComponents.PLAYER_AETHUM);
        if (!(aethum.tryConsumeAethum(aethum.maxAethum() * this.tier.data.abilityAethumCost))) {
            return TypedActionResult.pass(playerStack);
        }

        playerStack.set(ABILITY_START_TIME, world.getTime());
        return TypedActionResult.success(playerStack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (getAbilityTicks(world, stack) > this.tier.data.abilityDuration) {
            stack.remove(ABILITY_START_TIME);

            if (!(entity instanceof PlayerEntity player)) return;
            player.getItemCooldownManager().set(this, this.tier.data.abilityCooldown);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.affinity.staff.tooltip.consumption_per_use", Text.literal((int) (this.tier.data.abilityAethumCost * 100) + "%")));
    }

    public int abilityDuration() {
        return this.tier.data.abilityDuration;
    }

    public static boolean isBladeWithActiveAbility(World world, ItemStack stack, int minTier) {
        if (!(stack.getItem() instanceof ArtifactBladeItem blade)) return false;
        return getAbilityTicks(world, stack) >= 0 && blade.tier.ordinal() >= minTier;
    }

    public static int getAbilityTicks(World world, ItemStack stack) {
        if (!stack.contains(ABILITY_START_TIME)) return -1;
        return (int) (world.getTime() - stack.get(ABILITY_START_TIME));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (stack.getItem() instanceof ArtifactBladeItem blade && blade.tier == Tier.ASTRAL && attacker instanceof PlayerEntity player) {
            target.setHealth(0);
            target.onDeath(player.getDamageSources().playerAttack(player));
            return true;
        } else {
            return super.postHit(stack, target, attacker);
        }
    }

    private static MutableText makeInfiniteText() {
        var chars = Language.getInstance().get("text.affinity.infinite_attack_damage").toCharArray();
        float baseHue = (float) (System.currentTimeMillis() % 3000d) / 3000f;

        var outText = Text.literal(" ");

        for (int i = 0; i < chars.length; i++) {
            float hue = baseHue - i * .05f;
            if (hue < 0) hue += 1f;

            outText.append(TextOps.withColor(String.valueOf(chars[i]), MathHelper.hsvToRgb(hue, .8f, 1)));
        }

        return outText.append(Text.literal(" "));
    }

    @Environment(EnvType.CLIENT)
    private static void registerTooltipAddition() {
        ReplaceAttackDamageTextCallback.EVENT.register(stack -> {
            if (stack.getItem() instanceof ArtifactBladeItem blade && blade.tier == Tier.ASTRAL) {
                return makeInfiniteText();
            } else {
                return null;
            }
        });
    }

    static {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            registerTooltipAddition();
        }
    }

    public enum Tier implements ToolMaterial {
        FORGOTTEN(new TierData(
            500, 2, 20, 6, 7f, -2.4f, Rarity.UNCOMMON, 100, 300, .35f,
            ImmutableMultimap.of()
        )),
        STABILIZED(new TierData(
            1000, 3, 25, 8f, 10f, -2.4f, Rarity.UNCOMMON, 100, 300, .5f,
            ImmutableMultimap.<EntityAttribute, EntityAttributeModifier>builder()
                .put(AffinityEntityAttributes.NATURAL_AETHUM_REGEN_SPEED, new EntityAttributeModifier(Affinity.id("todo_needs_name_5"), .15, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))
                .build()
        )),
        STRENGTHENED(new TierData(
            1500, 4, 35, 11, 12f, -2f, Rarity.RARE, 160, 400, .65f,
            ImmutableMultimap.<EntityAttribute, EntityAttributeModifier>builder()
                .put(AffinityEntityAttributes.NATURAL_AETHUM_REGEN_SPEED, new EntityAttributeModifier(Affinity.id("todo_needs_name_4"), .35, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))
                .build()
        )),
        SUPERIOR(new TierData(
            3000, 5, 40, 15, 15f, -1.8f, Rarity.EPIC, 200, 800, .75f,
            ImmutableMultimap.<EntityAttribute, EntityAttributeModifier>builder()
                .put(AffinityEntityAttributes.NATURAL_AETHUM_REGEN_SPEED, new EntityAttributeModifier(Affinity.id("todo_needs_name_3"), .5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))
                .build()
        )),
        ASTRAL(new TierData(
            69000, 6, 100, 6969, 75f, 21f, Rarity.EPIC, 200, 800, 1f,
            ImmutableMultimap.<EntityAttribute, EntityAttributeModifier>builder()
                .put(AffinityEntityAttributes.MAX_AETHUM, new EntityAttributeModifier(Affinity.id("todo_needs_name_2"), 1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))
                .put(AffinityEntityAttributes.NATURAL_AETHUM_REGEN_SPEED, new EntityAttributeModifier(Affinity.id("todo_needs_name_1"), 3, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))
                .build()
        ));

        private final TierData data;

        Tier(TierData data) {
            this.data = data;
        }

        @Override
        public int getDurability() {
            return this.data.durability;
        }

        @Override
        public float getMiningSpeedMultiplier() {
            return this.data.miningSpeedMultiplier;
        }

        @Override
        public float getAttackDamage() {
            return this.data.attackDamage;
        }

        @Override
        public TagKey<Block> getInverseTag() {
            // TODO: ??? convert from mining level to inverse tag.
            return BlockTags.AIR;
        }

        @Override
        public int getEnchantability() {
            return this.data.enchantability;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }

        private record TierData(int durability, int miningLevel, int enchantability, float attackDamage,
                                float miningSpeedMultiplier, float attackSpeed,
                                Rarity rarity, int abilityDuration, int abilityCooldown, float abilityAethumCost,
                                Multimap<EntityAttribute, EntityAttributeModifier> modifiers) {}
    }
}
