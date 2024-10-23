package io.wispforest.affinity.item;

import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.aethumflux.net.MultiblockAethumNetworkMember;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.InquirableOutlineProvider;
import io.wispforest.affinity.blockentity.template.RitualCoreBlockEntity;
import io.wispforest.affinity.client.render.CuboidRenderer;
import io.wispforest.affinity.client.screen.FluxNetworkVisualizerScreen;
import io.wispforest.affinity.misc.InquiryQuestions;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.util.Delta;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WandOfInquiryItem extends Item implements DirectInteractionHandler {

    private static final Set<BlockPos> ACTIVE_OUTLINE_PROVIDERS = new HashSet<>();

    public WandOfInquiryItem() {
        super(AffinityItems.settings().maxCount(1));
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity instanceof ServerPlayerEntity player) {
            var params = MessageType.params(MessageType.MSG_COMMAND_INCOMING, player.getWorld().getRegistryManager(), stack.toHoverableText());
            var message = SentMessage.of(SignedMessage.ofUnsigned(InquiryQuestions.question()));

            player.sendChatMessage(message, false, params);
        }

        return entity instanceof PlayerEntity
            ? ActionResult.SUCCESS
            : ActionResult.PASS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        final var world = context.getWorld();
        final var player = context.getPlayer();
        final var pos = context.getBlockPos();

        final var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof RitualCoreBlockEntity core) {
            player.getItemCooldownManager().set(this, 30);

            if (!world.isClient()) {
                var setup = RitualCoreBlockEntity.examineSetup(core, !player.isSneaking());

                final double stability = !setup.isEmpty() ? setup.stability / 100 : 0;
                int stability20 = (int) Math.round(stability * 20);
                String stabilityBar = "|".repeat(stability20) + "§" + "|".repeat(20 - stability20);

                var text = TextOps.withColor("# §" + setup.socles.size() + " | §🛡 " + stabilityBar + " ",
                    0xD885A3, TextOps.color(Formatting.GRAY), 0x1572A1, TextOps.color(Formatting.GRAY));
                player.sendMessage(text, true);

                AffinityNetwork.CHANNEL.serverHandle(player).send(new SocleParticlesPacket(setup.socles.stream()
                    .map(RitualCoreBlockEntity.RitualSocleEntry::position).toList()));
            }

            return ActionResult.SUCCESS;
        } else if (blockEntity instanceof AethumNetworkMemberBlockEntity member) {
            if (member instanceof MultiblockAethumNetworkMember multiblock && !multiblock.isParent()) {
                if (!(multiblock.parent() instanceof AethumNetworkMemberBlockEntity parentMember)) {
                    return ActionResult.PASS;
                }
                member = parentMember;
            }

            if (world.isClient) {
                this.openVisualizerScreen(member);
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Environment(EnvType.CLIENT)
    private void openVisualizerScreen(AethumNetworkMemberBlockEntity member) {
        MinecraftClient.getInstance().setScreen(new FluxNetworkVisualizerScreen(member));
    }

    @Override
    public boolean shouldHandleInteraction(ItemStack stack, World world, BlockPos pos, BlockState state) {
        return Affinity.AETHUM_MEMBER.find(world, pos, null) != null;
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return handleAttackBlock(world, pos);
    }

    public static boolean handleAttackBlock(World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof InquirableOutlineProvider provider) {
            if (world.isClient) {
                if (!ACTIVE_OUTLINE_PROVIDERS.contains(pos) && provider.getActiveOutline() != null) {
                    ACTIVE_OUTLINE_PROVIDERS.add(pos);
                } else {
                    ACTIVE_OUTLINE_PROVIDERS.remove(pos);
                }
            }

            return false;
        } else {
            return true;
        }
    }

    static {
        //noinspection Convert2MethodRef
        AffinityNetwork.CHANNEL.registerClientbound(SocleParticlesPacket.class, (message, access) -> handleParticlePacket(message, access));
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) setupOutlineHandler();
    }

    @Environment(EnvType.CLIENT)
    private static void setupOutlineHandler() {
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            var player = MinecraftClient.getInstance().player;
            if (player == null || !player.isHolding(AffinityItems.WAND_OF_INQUIRY)) return;

            ACTIVE_OUTLINE_PROVIDERS.removeIf(providerPos -> {
                if (!(world.getBlockEntity(providerPos) instanceof InquirableOutlineProvider provider)) return true;

                var outline = provider.getActiveOutline();
                if (outline == null) return true;

                CuboidRenderer.add(providerPos, outline);
                return false;
            });
        });

        var thickness = new MutableFloat(0f);
        var lastOutlineBlock = new MutableObject<>(BlockPos.ORIGIN);

        var colorProgress = new MutableFloat(0f);
        var outlineColor = Color.ofRgb(0x0f132e);
        var activeOutlineColor = Color.ofRgb(0x2943a5);

        WorldRenderEvents.BLOCK_OUTLINE.register((worldContext, outlineContext) -> {
            var client = worldContext.gameRenderer().getClient();
            var pos = outlineContext.blockPos();
            var delta = client.getRenderTickCounter().getLastFrameDuration() * .25f;

            if (!pos.equals(lastOutlineBlock.getValue())) {
                thickness.setValue(0f);
                lastOutlineBlock.setValue(pos);
                colorProgress.setValue(targetColor(pos));
            }

            if (!client.player.isHolding(AffinityItems.WAND_OF_INQUIRY) || !(worldContext.world().getBlockEntity(pos) instanceof InquirableOutlineProvider)) {

                if (thickness.floatValue() >= .15f) {
                    thickness.add(Delta.compute(thickness.floatValue(), 0f, delta));
                } else {
                    return true;
                }
            } else {
                thickness.add(Delta.compute(thickness.floatValue(), 1f, delta));
            }

            var color = outlineColor.interpolate(activeOutlineColor, colorProgress.floatValue());
            colorProgress.add(Delta.compute(colorProgress.floatValue(), targetColor(pos), delta));

            var buffer = worldContext.consumers().getBuffer(CuboidRenderer.OUTLINE_LAYER);
            var matrices = worldContext.matrixStack();

            var shape = outlineContext.blockState().getOutlineShape(worldContext.world(), pos, ShapeContext.of(outlineContext.entity()));

            matrices.push();
            matrices.translate(pos.getX() - outlineContext.cameraX(), pos.getY() - outlineContext.cameraY(), pos.getZ() - outlineContext.cameraZ());

            shape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
                CuboidRenderer.line(matrices, buffer, (float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ, color, .01f * thickness.floatValue());
            });

            matrices.pop();

            return false;
        });
    }

    @Environment(EnvType.CLIENT)
    private static float targetColor(BlockPos pos) {
        return ACTIVE_OUTLINE_PROVIDERS.contains(pos) ? 1f : 0f;
    }

    @Environment(EnvType.CLIENT)
    private static void handleParticlePacket(SocleParticlesPacket message, ClientAccess access) {
        final var world = access.runtime().world;

        ClientParticles.persist();
        ClientParticles.setParticleCount(5);

        for (var soclePos : message.soclePositions()) {
            final var type = RitualSocleType.forBlockState(world.getBlockState(soclePos));
            final int color = type == null ? 0 : type.glowColor();

            ClientParticles.spawnPrecise(new DustParticleEffect(MathUtil.rgbToVec3f(color), 2), world,
                Vec3d.ofCenter(soclePos).add(0, .34, 0), .15, .15, .15);
        }

        ClientParticles.reset();
    }

    public record SocleParticlesPacket(List<BlockPos> soclePositions) {}
}
