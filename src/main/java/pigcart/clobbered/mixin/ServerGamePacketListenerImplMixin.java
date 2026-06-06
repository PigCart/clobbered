package pigcart.clobbered.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @WrapOperation(method = "handleAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"))
    public void dontDisconnect(ServerGamePacketListenerImpl instance, Component component, Operation<Void> original) {
        // no "Player {} tried to attack an invalid entity"
    }

    @WrapOperation(method = "handleAttack", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"))
    public void dontLogWarn(Logger instance, String s, Object object, Operation<Void> original) {
        // no!!!
    }
}
