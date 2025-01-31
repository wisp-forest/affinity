package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.ArcaneTreetapBlock;
import io.wispforest.affinity.blockentity.impl.HolographicStereopticonBlockEntity;
import io.wispforest.affinity.blockentity.impl.HolographicStereopticonBlockEntity.ImprintKind;
import io.wispforest.affinity.blockentity.impl.HolographicStereopticonBlockEntity.SectionData;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.misc.callback.ClientDoItemUseCallback;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityBlocks;
import io.wispforest.endec.SerializationContext;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.serialization.RegistriesAttribute;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static io.wispforest.affinity.blockentity.impl.HolographicStereopticonBlockEntity.IMPRINT_KIND_KEY_NAME;

public class HolographicStereopticonBlockItem extends BlockItem implements DirectInteractionHandler {

    @SuppressWarnings("rawtypes")
    public static final ComponentType<ImprintKind> IMPRINT_KIND = Affinity.component("holographic_stereopticon_imprint_kind", ImprintKind.ENDEC);
    public static final ComponentType<BlockPos> START_POS = Affinity.component("holographic_stereopticon_section_start_pos", MinecraftEndecs.BLOCK_POS);

    public HolographicStereopticonBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.empty());

        tooltip.add(Text.translatable(this.getTranslationKey() + ".cycle_imprint_kind_hint"));
        imprintKindOf(stack).appendTooltip(
                tooltip,
                Optional.ofNullable(stack.get(DataComponentTypes.BLOCK_ENTITY_DATA))
                        .map(component -> component.getNbt().getCompound(HolographicStereopticonBlockEntity.RENDERER_DATA_KEY))
                        .orElse(null),
                context.getRegistryLookup()
        );
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var playerStack = user.getStackInHand(hand);
        if (!user.isSneaking() || imprintKindOf(playerStack) != ImprintKind.SECTION) {
            return TypedActionResult.pass(playerStack);
        }

        this.doSectionClick(world, this.getSectionTarget(user), playerStack);
        return TypedActionResult.success(playerStack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var imprintKind = imprintKindOf(context.getStack());
        if (imprintKind != ImprintKind.BLOCK && imprintKind != ImprintKind.SECTION || !context.getPlayer().isSneaking()) {
            return super.useOnBlock(context);
        }

        if (imprintKind == ImprintKind.BLOCK) {
            var be = context.getWorld().getBlockEntity(context.getBlockPos());
            writeRendererData(
                    context.getStack(),
                    ImprintKind.BLOCK,
                    new HolographicStereopticonBlockEntity.BlockData(
                            context.getWorld().getBlockState(context.getBlockPos()),
                            be != null ? be.createNbtWithId(context.getWorld().getRegistryManager()) : null
                    ),
                    context.getWorld().getRegistryManager()
            );

            if (Affinity.onClient()) spawnBlockImprintEffects(context.getWorld(), context.getBlockPos());
        } else {
            this.doSectionClick(context.getWorld(), context.getBlockPos(), context.getStack());
        }

        return ActionResult.SUCCESS;
    }

    private void doSectionClick(World world, BlockPos pos, ItemStack stack) {
        var startPos = stack.get(START_POS);
        if (startPos == null) {
            stack.set(START_POS, pos);
        } else {
            stack.remove(START_POS);
            writeRendererData(stack, ImprintKind.SECTION, new SectionData(startPos, pos), world.getRegistryManager());
        }

        if (Affinity.onClient()) spawnBlockImprintEffects(world, pos);
    }

    private BlockPos getSectionTarget(Entity holder) {
        return ((BlockHitResult) holder.raycast(5, 0, false)).getBlockPos();
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType != ClickType.RIGHT || !otherStack.isEmpty()) return false;

        if (!stack.contains(DataComponentTypes.BLOCK_ENTITY_DATA)) {
            var nbt = new NbtCompound();
            nbt.putString("id", AffinityBlocks.Entities.HOLOGRAPHIC_STEREOPTICON.getRegistryEntry().getIdAsString());
            stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(nbt));
        }

        var nextImprintKind = imprintKindOf(stack).next();

        var ctx = SerializationContext.attributes(RegistriesAttribute.of(player.getRegistryManager()));
        var rendererData = new NbtCompound();
        rendererData.put(ctx, ImprintKind.ENDEC.keyed(IMPRINT_KIND_KEY_NAME, ImprintKind.BLOCK), nextImprintKind);

        stack.apply(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT, data -> {
            return data.apply(nbt -> nbt.put(HolographicStereopticonBlockEntity.RENDERER_DATA_KEY, rendererData));
        });
        return true;
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (imprintKindOf(stack) != ImprintKind.ITEM || clickType != ClickType.RIGHT || !slot.hasStack()) {
            return false;
        }

        writeRendererData(stack, ImprintKind.ITEM, slot.getStack(), player.getRegistryManager());
        return true;
    }

    @Override
    public boolean shouldHandleInteraction(ItemStack stack, World world, BlockPos pos, BlockState state) {
        var imprintKind = imprintKindOf(stack);
        return imprintKind == ImprintKind.BLOCK || imprintKind == ImprintKind.SECTION;
    }

    private static ImprintKind<?> imprintKindOf(ItemStack stack) {
        var data = getRendererData(stack);
        return data != null ? data.get(ImprintKind.ENDEC.keyed(IMPRINT_KIND_KEY_NAME, ImprintKind.BLOCK)) : ImprintKind.BLOCK;
    }

    private static @Nullable NbtCompound getRendererData(ItemStack stack) {
        var component = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
        if (component == null) return null;

        var nbt = component.getNbt();
        if (!nbt.contains(HolographicStereopticonBlockEntity.RENDERER_DATA_KEY, NbtElement.COMPOUND_TYPE)) return null;

        return nbt.getCompound(HolographicStereopticonBlockEntity.RENDERER_DATA_KEY);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!selected || imprintKindOf(stack) != ImprintKind.SECTION) return;

        BlockPos startPos, offset;

        NbtCompound rendererData;
        SectionData sectionData;
        if (stack.get(START_POS) == null
                && (rendererData = getRendererData(stack)) != null
                && (sectionData = ImprintKind.SECTION.readData(rendererData, world.getRegistryManager())) != null) {
            startPos = sectionData.start();
            offset = sectionData.end().subtract(sectionData.start());
        } else {
            if ((startPos = stack.get(START_POS)) == null) return;
            offset = this.getSectionTarget(entity).subtract(startPos);
        }

        var origin = new BlockPos(offset.getX() < 0 ? 1 : 0, offset.getY() < 0 ? 1 : 0, offset.getZ() < 0 ? 1 : 0);
        offset = offset.add(offset.getX() >= 0 ? 1 : 0, offset.getY() >= 0 ? 1 : 0, offset.getZ() >= 0 ? 1 : 0);

        CuboidRenderer.add(startPos, CuboidRenderer.Cuboid.of(origin, offset, Color.ofRgb(0x00FFAB), Color.WHITE));
    }

    @Environment(EnvType.CLIENT)
    private static void spawnBlockImprintEffects(World world, BlockPos pos) {
        world.playSound(pos.getX(), pos.getY(), pos.getZ(), world.getBlockState(pos).getSoundGroup().getPlaceSound(), SoundCategory.PLAYERS, 1f, 1f, false);

        ClientParticles.randomizeVelocity(.05f);
        ClientParticles.setParticleCount(10);
        ClientParticles.spawnCubeOutline(ParticleTypes.FIREWORK, world, Vec3d.of(pos).subtract(.05, .05, .05), 1.1f, .01f);
    }

    private static <T> void writeRendererData(ItemStack stack, ImprintKind<T> imprintKind, T data, RegistryWrapper.WrapperLookup registries) {
        var rendererData = new NbtCompound();
        var beTag = new NbtCompound();

        imprintKind.writeData(rendererData, data, registries);
        beTag.put(HolographicStereopticonBlockEntity.RENDERER_DATA_KEY, rendererData);
        beTag.putString("id", AffinityBlocks.Entities.HOLOGRAPHIC_STEREOPTICON.getRegistryEntry().getIdAsString());

        stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(beTag));
    }

    static {
        ClientDoItemUseCallback.EVENT.register((player, hand) -> {
            if (!player.isSneaking()) return ActionResult.PASS;

            var playerStack = player.getStackInHand(hand);
            if (!(playerStack.getItem() instanceof HolographicStereopticonBlockItem) || imprintKindOf(playerStack) != ImprintKind.ENTITY) {
                return ActionResult.PASS;
            }

            var entity = InteractionUtil.raycastEntities(player, 1f, 7, 1f, $ -> true);
            if (entity == null) return ActionResult.PASS;

            ClientParticles.setParticleCount(25);
            ClientParticles.spawn(ArcaneTreetapBlock.PARTICLE, player.getWorld(), MathUtil.entityCenterPos(entity.getEntity()), 1.25);

            ClientParticles.setParticleCount(20);
            ClientParticles.randomizeVelocity(.15);
            ClientParticles.spawn(ParticleTypes.FIREWORK, player.getWorld(), MathUtil.entityCenterPos(entity.getEntity()), 1.25);

            AffinityNetwork.CHANNEL.clientHandle().send(new CaptureEntityPacket(hand));
            return ActionResult.CONSUME;
        });

        AffinityNetwork.CHANNEL.registerServerbound(CaptureEntityPacket.class, (message, access) -> {
            var playerStack = access.player().getStackInHand(message.hand);
            if (!(playerStack.getItem() instanceof HolographicStereopticonBlockItem) || imprintKindOf(playerStack) != ImprintKind.ENTITY) {
                return;
            }

            var entity = InteractionUtil.raycastEntities(access.player(), 1f, 7, 1f, $ -> true);
            if (entity == null) return;

            writeRendererData(playerStack, ImprintKind.ENTITY, entity.getEntity(), access.runtime().getRegistryManager());
        });
    }

    public record CaptureEntityPacket(Hand hand) {}
}
