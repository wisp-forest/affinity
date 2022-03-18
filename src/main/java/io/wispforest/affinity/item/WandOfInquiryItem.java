package io.wispforest.affinity.item;

import io.wispforest.affinity.blockentity.impl.RitualCoreBlockEntity;
import io.wispforest.affinity.misc.util.MathUtil;
import io.wispforest.affinity.network.AffinityNetwork;
import io.wispforest.affinity.object.AffinityItems;
import io.wispforest.affinity.object.rituals.RitualSocleType;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.network.annotations.ElementType;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.particles.ClientParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class WandOfInquiryItem extends Item {

    public WandOfInquiryItem() {
        super(AffinityItems.settings(0).maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        final var world = context.getWorld();
        final var player = context.getPlayer();

        if (!(world.getBlockEntity(context.getBlockPos()) instanceof RitualCoreBlockEntity core))
            return ActionResult.PASS;

//        player.getItemCooldownManager().set(this, 100);

        if (!world.isClient()) {
            var configuration = core.examineConfiguration();

//            int stability10 = (int) Math.round(configuration.stability() / 10);
//            String stabilityBar = "▓".repeat(stability10) + "§" + "▒".repeat(10 - stability10);

            var text = TextOps.withColor("# §" + configuration.socles().size() + " | §⚡ §" + MathUtil.rounded(configuration.stability(), 2) + "%",
                    0x1572A1, TextOps.color(Formatting.GRAY), 0xD885A3, TextOps.color(Formatting.GRAY));
            player.sendMessage(text, true);

            AffinityNetwork.CHANNEL.serverHandle(player).send(new SocleParticlesPacket(configuration.socles().stream()
                    .map(RitualCoreBlockEntity.RitualSocleEntry::position).toList()));
        }

        return ActionResult.SUCCESS;
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

            ClientParticles.spawnPrecise(new DustParticleEffect(MathUtil.splitRGBToVector(color), 2), world,
                    Vec3d.ofCenter(soclePos).add(0, .34, 0), .15, .15, .15);
        }

        ClientParticles.reset();
    }

    public record SocleParticlesPacket(@ElementType(BlockPos.class) List<BlockPos> soclePositions) {
    }
}
