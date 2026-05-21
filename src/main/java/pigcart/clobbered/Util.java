package pigcart.clobbered;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.net.URI;
import java.util.stream.Stream;

public class Util {

    public static void hotbarMessage(Component component) {
        Minecraft.getInstance().gui.setOverlayMessage(component, false);
    }
    public static void hotbarMessage(Object o) {
        Minecraft.getInstance().gui.setOverlayMessage(Component.literal(String.valueOf(o)), false);
    }

    @SuppressWarnings("removal")
    public static Identifier getId(String path) {
        //? if <=1.20.1 {
        /*return new Identifier(Clobbered.MOD_ID, path);
        *///?} else {
        return Identifier.fromNamespaceAndPath(Clobbered.MOD_ID, path);
        //?}
    }
    @SuppressWarnings("removal")
    public static Identifier getMcId(String path) {
        //? if <=1.20.1 {
        /*return new Identifier(Identifier.DEFAULT_NAMESPACE, path);
        *///?} else {
        return Identifier.withDefaultNamespace(path);
        //?}
    }

    public static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> key) {
        //? if >=1.21.4 {
        return Minecraft.getInstance().level.registryAccess().lookupOrThrow(key);
        //?} else {
        /*return Minecraft.getInstance().level.registryAccess().registryOrThrow(key);
        *///?}
    }

    public static int getPixel(NativeImage img, int x, int y) {
        //? >=1.21.9 {
        return img.getPixel(x, y);
        //?} else {
        /*return img.getPixelRGBA(x, y);
        *///?}
    }

    public static void openUri(URI uri) {
        //? >=1.21.11 {
        net.minecraft.util.Util.getPlatform().openUri(uri);
        //?} else {
        /*net.minecraft.Util.getPlatform().openUri(uri);
        *///?}
    }

    public static Vec3 camPos(Camera cam) {
        //? >=1.21.11 {
        return cam.position();
        //?} else {
        /*return cam.getPosition();
        *///?}
    }

    public static Identifier getKeyId(ResourceKey key) {
        //? >=1.21.11 {
        return key.identifier();
        //?} else {
        /*return key.location();
         *///?}
    }

    static void addChatMsg(String message) {
        //? >=26.1 {
        Minecraft.getInstance().gui.getChat().addClientSystemMessage(Component.literal(message));
        //?} else {
        /*Minecraft.getInstance().gui.getChat().addMessage(Component.literal(message));
        *///?}
    }
}
