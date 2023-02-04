package io.wispforest.affinity.item;

import io.wispforest.affinity.aethumflux.net.MultiblockAethumNetworkMember;
import io.wispforest.affinity.blockentity.template.AethumNetworkMemberBlockEntity;
import io.wispforest.affinity.blockentity.template.RitualCoreBlockEntity;
import io.wispforest.affinity.client.screen.FluxNetworkVisualizerScreen;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class WandOfInquiryItem extends Item implements DirectInteractionHandler {

    public WandOfInquiryItem() {
        super(AffinityItems.settings(AffinityItemGroup.MAIN).maxCount(1));
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

                var text = TextOps.withColor("# §" + setup.socles.size() + " | §🛡 " + stabilityBar + "",
                        0xD885A3, TextOps.color(Formatting.GRAY), 0x1572A1, TextOps.color(Formatting.GRAY));
                player.sendMessage(text, true);

                AffinityNetwork.CHANNEL.serverHandle(player).send(new SocleParticlesPacket(setup.socles.stream()
                        .map(RitualCoreBlockEntity.RitualSocleEntry::position).toList()));
            }

            return ActionResult.SUCCESS;
        } else if (blockEntity instanceof AethumNetworkMemberBlockEntity member) {
            if (member instanceof MultiblockAethumNetworkMember multiblock && !multiblock.isParent()) return ActionResult.PASS;

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
    public boolean shouldHandleInteraction(World world, BlockPos pos, BlockState state) {
        return world.getBlockEntity(pos) instanceof RitualCoreBlockEntity;
    }

    static {
        //noinspection Convert2MethodRef
        AffinityNetwork.CHANNEL.registerClientbound(SocleParticlesPacket.class, (message, access) -> handleParticlePacket(message, access));
    }

    @Environment(EnvType.CLIENT)
    private static void handleParticlePacket(SocleParticlesPacket message, ClientAccess access) {
        final var world = access.runtime().world;

        ClientParticles.persist();
        ClientParticles.setParticleCount(5);

        for (var soclePos : message.soclePositions()) {
            final var type = RitualSocleType.forBlockState(world.getBlockState(soclePos));
            final int color = type == null ? 0 : type.glowColor();

            ClientParticles.spawnPrecise(new DustParticleEffect(MathUtil.splitRGBToVec3f(color), 2), world,
                    Vec3d.ofCenter(soclePos).add(0, .34, 0), .15, .15, .15);
        }

        ClientParticles.reset();
    }

    public record SocleParticlesPacket(List<BlockPos> soclePositions) {}
}
