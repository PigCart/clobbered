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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

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
    public void extractRenderState(LobbedItem lobbedItem, LobbedItemRenderState state, float partialTicks) {
        state.animOffset = this.animOffset;
        state.useEntityRotation = lobbedItem.isHurled() || lobbedItem.isImpaling();
        if (lobbedItem.isInEntity()) { // rotate with the entity and position accordingly
            state.xRot = lobbedItem.getEntityData().get(LobbedItem.IMPALE_ROT_X);
            float impaledBodyRot = lobbedItem.impaledEntity.getPreciseBodyRotation(partialTicks);
            state.yRot = impaledBodyRot + lobbedItem.getEntityData().get(LobbedItem.IMPALE_ROT_Y);
            Vec3 offset = Vec3.applyLocalCoordinatesToRotation(new Vec2(0, impaledBodyRot), lobbedItem.getImpaleOffset());
            lobbedItem.setPos(lobbedItem.impaledEntity.position().add(offset));
        } else {
            state.xRot = lobbedItem.getXRot(partialTicks); // pitch
            state.yRot = lobbedItem.getYRot(partialTicks); // yaw
        }
        super.extractRenderState(lobbedItem, state, partialTicks);
        state.extractItemGroupRenderState(lobbedItem, lobbedItem.getRenderItemStack(), this.itemModelResolver);
        // use held item model instead of dropped item model
        if (lobbedItem.isHurled()) itemModelResolver.updateForTopItem(state.item,
                lobbedItem.getRenderItemStack(),
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                lobbedItem.level(),
                lobbedItem,
                0);
    }

    @Override
    public void submit(LobbedItemRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.item.isEmpty()) {
            poseStack.pushPose();
            AABB boundingBox = state.item.getModelBoundingBox();
            if (state.useEntityRotation) {
                float scale = -0.4F;
                Vec3 off = Vec3.directionFromRotation(state.xRot, state.yRot).multiply(scale, scale, scale);
                poseStack.translate(off.add(0, 0.1, 0));
                // do rotation after translation
                poseStack.mulPose(Axis.YN.rotationDegrees(state.yRot));
                poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot + 100));
            } else { // use regular dropped item spin animation
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
        boolean useEntityRotation;
        public float xRot;
        public float yRot;
    }
}
