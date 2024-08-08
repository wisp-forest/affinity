package io.wispforest.affinity.client.render.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.affinity.Affinity;
import io.wispforest.affinity.block.impl.HolographicStereopticonBlock;
import io.wispforest.affinity.blockentity.impl.HolographicStereopticonBlockEntity;
import io.wispforest.affinity.client.AffinityClient;
import io.wispforest.affinity.client.render.BasicVertexConsumerProvider;
import io.wispforest.affinity.client.render.PostEffectBuffer;
import io.wispforest.affinity.misc.MixinHooks;
import io.wispforest.affinity.misc.quack.AffinityClientWorldExtension;
import io.wispforest.affinity.mixin.client.WorldRendererAccessor;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.util.Delta;
import io.wispforest.worldmesher.WorldMesh;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Cleaner;

public class HolographicStereopticonBlockEntityRenderer extends AffinityBlockEntityRenderer<HolographicStereopticonBlockEntity> {

    private static final Cleaner MESH_CLEANER = Cleaner.create();

    private static final PostEffectBuffer BUFFER = new PostEffectBuffer();
    private static final BasicVertexConsumerProvider VERTEX_CONSUMERS = new BasicVertexConsumerProvider(4096);

    private static boolean rendering = false;

    public HolographicStereopticonBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    protected void render(HolographicStereopticonBlockEntity entity, float tickDelta, float frameDelta, long time, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var rotationOffset = entity.getCachedState().get(HolographicStereopticonBlock.FACING).asRotation();

        var delegate = entity.renderer();
        if (delegate != Renderer.EMPTY && delegate.ready()) {
            entity.visualRenderScale += Delta.compute(entity.visualRenderScale, entity.renderScale(), frameDelta);

            entity.currentRotation = entity.spin()
                    ? (entity.currentRotation + frameDelta * .15f) % 360f
                    : entity.currentRotation + Delta.compute(entity.currentRotation, entity.currentRotation < 180 ? 0 : 360, frameDelta * .1f);
        }

        var nested = rendering;
        if (!nested) {
            BUFFER.beginWrite(false, 0);
            matrices.push();

            rendering = true;
        }

        delegate.render(entity.visualRenderScale, entity.currentRotation - rotationOffset, matrices, VERTEX_CONSUMERS, tickDelta, light, overlay);
        VERTEX_CONSUMERS.draw();

        if (!nested) {
            matrices.pop();
            BUFFER.endWrite();

            rendering = false;
        }
    }

    @Override
    public boolean rendersOutsideBoundingBox(HolographicStereopticonBlockEntity beaconBlockEntity) {
        return true;
    }

    static {
        BUFFER.setBlitProgram(AffinityClient.DEPTH_MERGE_BLIT_PROGRAM::program);

        WorldRenderEvents.START.register(context -> BUFFER.clear());
        WorldRenderEvents.END.register(context -> {
            AffinityClient.DEPTH_MERGE_BLIT_PROGRAM.setupSamplers(BUFFER.buffer().getDepthAttachment());
            BUFFER.draw(new Color(1f, 1f, 1f, .75f));
        });
    }

    public interface Renderer {
        Renderer EMPTY = (scale, rotation, matrices, vertexConsumers, tickDelta, light, overlay) -> {};

        void render(float scale, float rotation, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, int light, int overlay);

        default boolean ready() {
            return true;
        }

        default int updateDelay() {
            return 0;
        }

        static Renderer read(HolographicStereopticonBlockEntity stereopticon, @Nullable NbtCompound nbt) {
            if (nbt == null) return EMPTY;
            return switch (nbt.getString(HolographicStereopticonBlockEntity.IMPRINT_KIND_KEY_NAME)) {
                case "item" -> {
                    var client = MinecraftClient.getInstance();
                    var item = HolographicStereopticonBlockEntity.ImprintKind.ITEM.readData(nbt, stereopticon.getWorld().getRegistryManager());
                    if (item == null) yield EMPTY;

                    yield (scale, rotation, matrices, vertexConsumers, tickDelta, light, overlay) -> {
                        matrices.translate(.5f, .75f, .5f);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
                        matrices.scale(scale, scale, scale);

                        matrices.translate(0, client.getItemRenderer().getModel(item, client.world, null, 0).hasDepth() ? -.05 : .125, 0);

                        client.getItemRenderer().renderItem(item, ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, client.world, 0);
                    };
                }
                case "block" -> {
                    var client = MinecraftClient.getInstance();
                    var data = HolographicStereopticonBlockEntity.ImprintKind.BLOCK.readData(nbt, stereopticon.getWorld().getRegistryManager());
                    if (data == null) yield EMPTY;

                    BlockEntity entity;
                    if (data.nbt() != null) {
                        entity = BlockEntity.createFromNbt(stereopticon.getPos(), data.state(), data.nbt(), stereopticon.getWorld().getRegistryManager());
                        entity.setWorld(client.world);
                    } else {
                        entity = null;
                    }

                    yield (scale, rotation, matrices, vertexConsumers, tickDelta, light, overlay) -> {
                        matrices.translate(.5f, .75f, .5f);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
                        matrices.scale(.5f * scale, .5f * scale, .5f * scale);
                        matrices.translate(-.5f, 0, -.5f);

                        client.getBlockRenderManager().renderBlockAsEntity(data.state(), matrices, vertexConsumers, light, overlay);
                        if (entity != null) {
                            client.getBlockEntityRenderDispatcher().renderEntity(entity, matrices, vertexConsumers, light, overlay);
                        }
                    };
                }
                case "entity" -> {
                    var client = MinecraftClient.getInstance();
                    var entity = HolographicStereopticonBlockEntity.ImprintKind.ENTITY.readData(nbt, stereopticon.getWorld().getRegistryManager());
                    if (entity == null) yield EMPTY;

                    entity.updatePosition(stereopticon.getPos().getX(), stereopticon.getPos().getY(), stereopticon.getPos().getZ());

                    yield (scale, rotation, matrices, vertexConsumers, tickDelta, light, overlay) -> {
                        matrices.translate(.5f, .75f, .5f);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
                        matrices.scale(.35f * scale, .35f * scale, .35f * scale);

                        client.getEntityRenderDispatcher().setRenderShadows(false);
                        client.getEntityRenderDispatcher().render(entity, 0, 0, 0, 0, 0, matrices, vertexConsumers, light);
                        client.getEntityRenderDispatcher().setRenderShadows(true);
                    };
                }
                case "section" -> {
                    var client = MinecraftClient.getInstance();

                    var data = HolographicStereopticonBlockEntity.ImprintKind.SECTION.readData(nbt, stereopticon.getWorld().getRegistryManager());
                    if (data == null) yield EMPTY;

                    var mesh = new WorldMesh.Builder(client.world, data.start(), data.end()).build();
                    var updateDelay = (int) (mesh.dimensions().getLengthX() * mesh.dimensions().getLengthY() * mesh.dimensions().getLengthZ()) / 20000;

                    var realMeshDimensions = Box.enclosing(mesh.startPos(), mesh.endPos().add(1, 1, 1));
                    AffinityClientWorldExtension.BlockUpdateListener listener = (pos, from, to) -> {
                        if (!realMeshDimensions.contains(pos.getX(), pos.getY(), pos.getZ())) return true;
                        if (!client.getBakedModelManager().shouldRerender(from, to)) return true;

                        stereopticon.refreshRenderer();
                        return false;
                    };
                    client.send(() -> ((AffinityClientWorldExtension) client.world).affinity$addBlockUpdateListener(listener));

                    var renderer = new Renderer() {

                        private static int recursionDepth = 0;
                        private final AffinityClientWorldExtension.BlockUpdateListener noGcPlease = listener;

                        @Override
                        public void render(float scale, float rotation, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, int light, int overlay) {
                            if (!mesh.canRender()) return;

                            matrices.translate(.5f, .75f, .5f);
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));

                            float meshScale = Math.min(.75f, 3f / (int) Math.max(mesh.dimensions().getLengthX(), Math.max(mesh.dimensions().getLengthY(), mesh.dimensions().getLengthZ())));
                            matrices.scale(meshScale * scale, meshScale * scale, meshScale * scale);
                            matrices.translate(-mesh.dimensions().getLengthX() / 2, 0, -mesh.dimensions().getLengthZ() / 2);

                            if (Affinity.config().renderBlockEntitiesInStereopticonSectionImprints()) {
                                MixinHooks.forceBlockEntityRendering = true;
                                mesh.renderInfo().blockEntities().forEach((blockPos, entity) -> {
                                    if (entity instanceof HolographicStereopticonBlockEntity && recursionDepth > Affinity.config().stereopticonSectionImprintRecursionLimit() - 1) {
                                        return;
                                    }

                                    matrices.push();
                                    matrices.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());

                                    recursionDepth++;
                                    client.getBlockEntityRenderDispatcher().render(entity, tickDelta, matrices, vertexConsumers);
                                    recursionDepth--;

                                    matrices.pop();
                                });
                                MixinHooks.forceBlockEntityRendering = false;
                            }

                            if (Affinity.config().renderEntitiesInStereopticonSectionImprints()) {
                                client.world.getOtherEntities(null, realMeshDimensions).forEach(entity -> {
                                    var entityVisible = ((WorldRendererAccessor) client.worldRenderer).affinity$getFrustum() != null && client.getEntityRenderDispatcher().shouldRender(
                                            entity,
                                            ((WorldRendererAccessor) client.worldRenderer).affinity$getFrustum(),
                                            client.getEntityRenderDispatcher().camera.getPos().x,
                                            client.getEntityRenderDispatcher().camera.getPos().y,
                                            client.getEntityRenderDispatcher().camera.getPos().z
                                    );

                                    var pos = entity.getLerpedPos(tickDelta).subtract(mesh.startPos().getX(), mesh.startPos().getY(), mesh.startPos().getZ());
                                    client.getEntityRenderDispatcher().render(entity, pos.x, pos.y, pos.z, entity.getYaw(tickDelta), entityVisible ? tickDelta : 0, matrices, vertexConsumers, light);
                                });
                            }

                            var meshViewStack = new MatrixStack();
                            meshViewStack.peek().getPositionMatrix().set(RenderSystem.getModelViewMatrix());
                            meshViewStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());

                            mesh.render(meshViewStack);
                        }

                        @Override
                        public boolean ready() {
                            return mesh.canRender();
                        }

                        @Override
                        public int updateDelay() {
                            return updateDelay;
                        }
                    };

                    MESH_CLEANER.register(renderer, () -> MinecraftClient.getInstance().execute(mesh::reset));
                    yield renderer;
                }
                default -> EMPTY;
            };
        }
    }
}