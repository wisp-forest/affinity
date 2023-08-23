package io.wispforest.affinity.item;

import io.wispforest.affinity.block.impl.ArcaneTreetapBlock;
import io.wispforest.affinity.blockentity.impl.HolographicStereopticonBlockEntity;
import io.wispforest.affinity.blockentity.impl.HolographicStereopticonBlockEntity.ImprintKind;
import io.wispforest.affinity.blockentity.impl.HolographicStereopticonBlockEntity.SectionData;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.misc.callback.ClientDoItemUseCallback;
import io.wispforest.affinity.misc.NbtQuery;
import io.wispforest.affinity.misc.util.InteractionUtil;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
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

import static io.wispforest.affinity.blockentity.impl.HolographicStereopticonBlockEntity.IMPRINT_KIND_KEY_NAME;

public class HolographicStereopticonBlockItem extends BlockItem implements DirectInteractionHandler {

    public static final NbtKey<ImprintKind<?>> IMPRINT_KIND_KEY = new NbtKey<>(IMPRINT_KIND_KEY_NAME, NbtKey.Type.STRING.then(ImprintKind::byId, kind -> kind.id));
    public static final NbtKey<NbtCompound> BLOCK_ENTITY_TAG_KEY = new NbtKey<>("BlockEntityTag", NbtKey.Type.COMPOUND);
    public static final NbtKey<BlockPos> START_POS_KEY = new NbtKey<>("SectionStartPos", NbtKey.Type.LONG.then(BlockPos::fromLong, BlockPos::asLong));

    private static final NbtQuery<NbtCompound> RENDERER_DATA_QUERY = NbtQuery.begin().key("BlockEntityTag").key("RendererData").compound();

    public HolographicStereopticonBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.empty());

        tooltip.add(Text.translatable(this.getTranslationKey() + ".cycle_imprint_kind_hint"));
        imprintKindOf(stack).appendTooltip(tooltip, RENDERER_DATA_QUERY.get(stack.getNbt()));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var playerStack = user.getStackInHand(hand);
        if (!user.isSneaking() || imprintKindOf(playerStack) != ImprintKind.SECTION) return TypedActionResult.pass(playerStack);

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
                            be != null ? be.createNbtWithId() : null
                    )
            );

            spawnBlockImprintEffects(context.getWorld(), context.getBlockPos());
        } else {
            this.doSectionClick(context.getWorld(), context.getBlockPos(), context.getStack());
        }

        return ActionResult.SUCCESS;
    }

    private void doSectionClick(World world, BlockPos pos, ItemStack stack) {
        var startPos = stack.getOr(START_POS_KEY, null);
        if (startPos == null) {
            stack.put(START_POS_KEY, pos);
        } else {
            stack.delete(START_POS_KEY);
            writeRendererData(stack, ImprintKind.SECTION, new SectionData(startPos, pos));
        }

        spawnBlockImprintEffects(world, pos);
    }

    private BlockPos getSectionTarget(Entity holder) {
        return ((BlockHitResult) holder.raycast(5, 0, false)).getBlockPos();
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType != ClickType.RIGHT || !otherStack.isEmpty()) return false;

        if (!stack.has(BLOCK_ENTITY_TAG_KEY)) {
            stack.put(BLOCK_ENTITY_TAG_KEY, new NbtCompound());
        }

        var rendererData = RENDERER_DATA_QUERY.get(stack.getNbt());
        var nextImprintKind = (rendererData != null ? rendererData.get(IMPRINT_KIND_KEY) : ImprintKind.BLOCK).next();

        rendererData = new NbtCompound();
        rendererData.put(IMPRINT_KIND_KEY, nextImprintKind);

        stack.get(BLOCK_ENTITY_TAG_KEY).put(HolographicStereopticonBlockEntity.RENDERER_DATA_KEY, rendererData);
        return true;
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (imprintKindOf(stack) != ImprintKind.ITEM || clickType != ClickType.RIGHT || !slot.hasStack()) {
            return false;
        }

        writeRendererData(stack, ImprintKind.ITEM, slot.getStack());
        return true;
    }

    @Override
    public boolean shouldHandleInteraction(ItemStack stack, World world, BlockPos pos, BlockState state) {
        var imprintKind = imprintKindOf(stack);
        return imprintKind == ImprintKind.BLOCK || imprintKind == ImprintKind.SECTION;
    }

    private static ImprintKind<?> imprintKindOf(ItemStack stack) {
        var data = RENDERER_DATA_QUERY.get(stack.getNbt());
        return data != null ? data.get(IMPRINT_KIND_KEY) : ImprintKind.BLOCK;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!selected || imprintKindOf(stack) != ImprintKind.SECTION) return;

        BlockPos startPos, offset;

        NbtCompound rendererData;
        SectionData sectionData;
        if (stack.getOr(START_POS_KEY, null) == null
                && (rendererData = RENDERER_DATA_QUERY.get(stack.getNbt())) != null
                && (sectionData = ImprintKind.SECTION.readData(rendererData)) != null) {
            startPos = sectionData.start();
            offset = sectionData.end().subtract(sectionData.start());
        } else {
            if ((startPos = stack.getOr(START_POS_KEY, null)) == null) return;
            offset = this.getSectionTarget(entity).subtract(startPos);
        }

        var origin = new BlockPos(offset.getX() < 0 ? 1 : 0, offset.getY() < 0 ? 1 : 0, offset.getZ() < 0 ? 1 : 0);
        offset = offset.add(offset.getX() >= 0 ? 1 : 0, offset.getY() >= 0 ? 1 : 0, offset.getZ() >= 0 ? 1 : 0);

        CuboidRenderer.add(startPos, CuboidRenderer.Cuboid.of(origin, offset, Color.ofRgb(0x00FFAB), Color.WHITE));
    }

    private static void spawnBlockImprintEffects(World world, BlockPos pos) {
        WorldOps.playSound(world, pos, world.getBlockState(pos).getSoundGroup().getPlaceSound(), SoundCategory.PLAYERS);

        ClientParticles.randomizeVelocity(.05f);
        ClientParticles.setParticleCount(10);
        ClientParticles.spawnCubeOutline(ParticleTypes.FIREWORK, world, Vec3d.of(pos).subtract(.05, .05, .05), 1.1f, .01f);
    }

    private static <T> void writeRendererData(ItemStack stack, ImprintKind<T> imprintKind, T data) {
        var rendererData = new NbtCompound();
        var beTag = new NbtCompound();

        imprintKind.writeData(rendererData, data);
        beTag.put(HolographicStereopticonBlockEntity.RENDERER_DATA_KEY, rendererData);

        stack.getOrCreateNbt().put("BlockEntityTag", beTag);
    }

    static {
        ClientDoItemUseCallback.EVENT.register((player, hand) -> {
            if (!player.isSneaking()) return ActionResult.PASS;

            var playerStack = player.getStackInHand(hand);
            if (!(playerStack.getItem() instanceof HolographicStereopticonBlockItem) || imprintKindOf(playerStack) != ImprintKind.ENTITY) {
                return ActionResult.PASS;
            }

            var entity = InteractionUtil.raycastEntities(player, 7, 1f, $ -> true);
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

            var entity = InteractionUtil.raycastEntities(access.player(), 7, 1f, $ -> true);
            if (entity == null) return;

            writeRendererData(playerStack, ImprintKind.ENTITY, entity.getEntity());
        });
    }

    public record CaptureEntityPacket(Hand hand) {}
}
