package pigcart.clobbered.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.clobbered.Util;
import pigcart.clobbered.networking.LobItemServerboundPayload;

import static pigcart.clobbered.ClobberedClient.throwStrength;
import static pigcart.clobbered.config.ConfigManager.config;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    @Final
    public Options options;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    public abstract boolean hasControlDown();

    @Unique
    public boolean shouldDrop;

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    public void handleDropKeybinds(CallbackInfo ci) {
        if (!this.player.isSpectator()) {
            if (this.options.keyDrop.isDown()) {
                throwStrength++;
                StringBuilder meter = new StringBuilder();
                meter.append("|".repeat(Math.min(throwStrength, config.maximumDropStrength)));
                MutableComponent component = Component.literal(meter.toString());
                if (throwStrength >= config.maximumDropStrength) component.withStyle(ChatFormatting.GRAY);
                if (throwStrength > 4 && config.showStrengthBar) Util.hotbarMessage(component);
                shouldDrop = true;
            } else if (shouldDrop) {
                Util.hotbarMessage(Component.literal(""));
                throwStrength = Math.clamp(throwStrength, config.minimumDropStrength, config.maximumDropStrength);
                boolean dropAll = this.hasControlDown();
                LobItemServerboundPayload payload = new LobItemServerboundPayload(throwStrength, dropAll);
                ClientPlayNetworking.send(payload);
                ItemStack prediction = this.player.getInventory().removeFromSelected(dropAll);
                if (!prediction.isEmpty()) this.player.swing(InteractionHand.MAIN_HAND);
                throwStrength = 0;
                shouldDrop = false;
            }
        }
    }

    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;drop(Z)Z"))
    public boolean preventDrop(LocalPlayer instance, boolean all, Operation<Boolean> original) {
        return false;
    }
}
