package io.wispforest.affinity.client.misc;

import io.wispforest.worldmesher.WorldMesh;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.function.BooleanSupplier;

@Environment(EnvType.CLIENT)
public class WorldMeshUtil {

    public static HitResult pickRay(WorldMesh mesh, BlockView world, Matrix4f projection, Matrix4f viewMatrix, double mouseX, double mouseY, BooleanSupplier shouldRaycast) {
        if (!shouldRaycast.getAsBoolean()) return BlockHitResult.createMissed(Vec3d.ZERO, Direction.NORTH, BlockPos.ORIGIN);

        var window = MinecraftClient.getInstance().getWindow();
        float x = (float) ((2f * window.getScaleFactor() * mouseX) / window.getFramebufferWidth() - 1f);
        float y = (float) (1f - (2f * window.getScaleFactor() * mouseY) / window.getFramebufferHeight());

        // Unproject and compute ray enter/exit positions

        var invProj = new Matrix4f(projection).invert();
        var invView = new Matrix4f(viewMatrix).invert();

        var near = new Vector4f(x, y, -1, 1).mul(invProj).mul(invView);
        var far = new Vector4f(x, y, 1, 1).mul(invProj).mul(invView);

        // Since the ray points we get are at bogus positions very, very far away due to the
        // orthographic projection we compute the ray and move the points in by 20% and 65% respectively
        // to eliminate about 85% of the problem space. This is a fine optimization to make since
        // no real network will (or even can) ever extend over 20k blocks
        //
        // This results in about a 2x performance gain (170 -> 350FPS on my machine)
        // glisco, 07.02.2023

        var ray = new Vector4f(far).sub(near);

        near.add(ray.mul(.2f));
        far.add(ray.mul(1f / .2f * -.65f));

        // Now that we have somewhat sane ray points, we just hand off to vanilla raycasting
        // in the masked block render view we created for meshing the network in the first place

        var origin = mesh.startPos();
        return world.raycast(new RaycastContext(
            new Vec3d(origin.getX() + near.x, origin.getY() + near.y, origin.getZ() + near.z),
            new Vec3d(origin.getX() + far.x, origin.getY() + far.y, origin.getZ() + far.z),
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            MinecraftClient.getInstance().player
        ));
    }

}
