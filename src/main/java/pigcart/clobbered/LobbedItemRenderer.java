package pigcart.clobbered;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public class LobbedItemRenderer extends EntityRenderer<LobbedItem, LobbedItemRenderer.LobbedItemRenderState> {

    private final ItemModelResolver itemModelResolver;
    private final RandomSource random = RandomSource.create();
    public final float animOffset = this.random.nextFloat() * (float) Math.PI * 2.0F;

    public LobbedItemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public LobbedItemRenderState createRenderState() {
        return new LobbedItemRenderState();
    }

    @Override
    public void extractRenderState(LobbedItem item, LobbedItemRenderState state, float partialTicks) {
        state.animOffset = this.animOffset;
        state.hurled = item.isHurled();
        state.impaling = item.isImpaling();
        if (item.isImpalingEntity()) {
            state.impaledRot = item.impaledEntity.getPreciseBodyRotation(partialTicks);
            item.setPos(item.impaledEntity.position().add(item.getImpaleOffset()));
        }
        state.velocity = item.getDeltaMovement().toVector3f().add(0.01F, 0, 0);
        state.thrownRotation = item.getEntityData().get(LobbedItem.ROTATION_YAW);
        // if impaling entity, add entity rotation to impale rot.
        state.yRot = item.getYRot(partialTicks);
        state.xRot = item.getXRot(partialTicks);
        super.extractRenderState(item, state, partialTicks);
        state.extractItemGroupRenderState(item, item.getRenderItemStack(), this.itemModelResolver);
    }

    @Override
    public void submit(LobbedItemRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.item.isEmpty()) {
            poseStack.pushPose();
            AABB boundingBox = state.item.getModelBoundingBox();
            // scale hurled items to held size
            if (state.hurled) {
                float scale = 1.5F;
                poseStack.scale(scale, scale, scale);
            }
            // set rotation
            if (state.impaling || state.hurled) {
                //poseStack.mulPose(Axis.YN.rotationDegrees(state.thrownRotation));
                poseStack.mulPose(Axis.YN.rotationDegrees(state.yRot - 90F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot + 135F));
            } else {
                float minOffsetY = -((float)boundingBox.minY) + 0.0625F;
                float bob = Mth.sin(state.ageInTicks / 10.0F + state.animOffset) * 0.1F + 0.1F;
                poseStack.translate(0.0F, bob + minOffsetY, 0.0F);
                float spin = ItemEntity.getSpin(state.ageInTicks, state.animOffset);
                poseStack.mulPose(Axis.YP.rotation(spin));
            }
            ItemEntityRenderer.submitMultipleFromCount(poseStack, submitNodeCollector, state.lightCoords, state, this.random, boundingBox);
            poseStack.popPose();
            super.submit(state, poseStack, submitNodeCollector, camera);
        }
    }

    public static class LobbedItemRenderState extends ItemClusterRenderState {
        float animOffset;
        boolean hurled;
        boolean impaling;
        float impaledRot;
        Vector3f velocity;
        float thrownRotation;
        public float xRot;
        public float yRot;
    }
}
