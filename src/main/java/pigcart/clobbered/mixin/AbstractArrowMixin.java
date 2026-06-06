package pigcart.clobbered.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pigcart.clobbered.Clobbered;

import static pigcart.clobbered.config.ConfigManager.config;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends EntityMixin {

    @Shadow
    protected abstract boolean isInGround();

    @Shadow
    public AbstractArrow.Pickup pickup;

    @Shadow
    protected abstract ItemStack getPickupItem();

    @Unique
    public void drop(int pickUpDelay) {
        drop(pickUpDelay, this.getX(), this.getY(), this.getZ());
    }
    @Unique
    public void drop(int pickUpDelay, double x, double y, double z) {
        ItemEntity itemEntity = new ItemEntity(this.level(), x, y, z, getPickupItem());
        itemEntity.setPickUpDelay(pickUpDelay);
        this.level().addFreshEntity(itemEntity);
        this.discard();
    }

    @WrapMethod(method = "tryPickup")
    protected boolean tryPickup(Player player, Operation<Boolean> original) {
        if (!config.manualArrowPickUp) {
            return this.pickup == AbstractArrow.Pickup.DISALLOWED ? config.pickUpMobArrows : original.call(player);
        }
        return false;
    }

    @WrapMethod(method = "isPickable")
    public boolean isPickable(Operation<Boolean> original) {
        if (config.manualArrowPickUp) {
            return this.isInGround();
        }
        return original.call();
    }

    @Override
    public InteractionResult hookInteract(Player player, InteractionHand hand, Vec3 location, Operation<InteractionResult> original) {
        if (this.getPickupItem().is(Clobbered.UNCATCHABLE)) return InteractionResult.PASS;
        if (player.getItemInHand(hand).isEmpty()) {
            if (this.level().isClientSide()) { // do pick-up animation
                EntityRenderState itemState = Minecraft.getInstance().getEntityRenderDispatcher().extractEntity((AbstractArrow)(Object)this, 1.0F);
                Minecraft.getInstance().particleEngine.add(
                        new ItemPickupParticle((ClientLevel) this.level(), itemState, player, this.getDeltaMovement())
                );
            } else {
                player.setItemInHand(hand, this.getPickupItem());
                this.discard();
            }
        } else {
            if (!level().isClientSide()) this.drop(0);
        }
        return InteractionResult.SUCCESS;
    }
}
