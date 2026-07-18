//? if neoforge {
/*package pigcart.clobbered.loaders.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.lwjgl.glfw.GLFW;
import pigcart.clobbered.Clobbered;
import pigcart.clobbered.ClobberedClient;
import pigcart.clobbered.LobbedItemRenderer;
import pigcart.clobbered.Util;
import pigcart.clobbered.config.gui.ConfigScreen;
import pigcart.clobbered.networking.KickServerboundPayload;
import pigcart.clobbered.networking.LobItemServerboundPayload;

import static pigcart.clobbered.Clobbered.*;

@Mod(MOD_ID)
public class NeoforgeEntrypoint {

    public NeoforgeEntrypoint(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(NeoforgeEntrypoint::registerClientCommands);
        NeoForge.EVENT_BUS.addListener(NeoforgeEntrypoint::onClientTick);
        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (modContainer, parent) -> ConfigScreen.get(parent)
        );
        eventBus.addListener(RegisterEvent.class, NeoforgeEntrypoint::register);
        eventBus.addListener(NeoforgeEntrypoint::registerEntityRenderers);
        eventBus.addListener(NeoforgeEntrypoint::registerPayloadHandlers);
        eventBus.addListener(NeoforgeEntrypoint::registerCreativeTabContents);
    }

    public static void register(RegisterEvent event) {
        event.register(Registries.ITEM, h -> {
            h.register(Util.getId("boomerang"), BOOMERANG_ITEM);
        });
        event.register(Registries.ENTITY_TYPE, h -> {
            h.register(LOBBED_ITEM_ID, LOBBED_ITEM);
            Clobbered.onInitialize();
        });
    }

    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(ClobberedClient.getCommands());
    }
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(LOBBED_ITEM, LobbedItemRenderer::new);
    }
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(LobItemServerboundPayload.TYPE, LobItemServerboundPayload.CODEC,
                (payload, ctx) -> Clobbered.handleLobItem(payload, (ServerPlayer) ctx.player())
        );
        registrar.playToServer(KickServerboundPayload.TYPE, KickServerboundPayload.CODEC,
                (payload, ctx) -> Clobbered.handleKick(payload, (ServerPlayer) ctx.player())
        );
    }
    public static void registerCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(BOOMERANG_ITEM);
        }
    }
    public static final Lazy<KeyMapping> KICK_KEY = Lazy.of(() ->
            new KeyMapping(
                    "key.clobbered.kick",
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_C,
                    KeyMapping.Category.register(Util.getId("keybinds"))
            )
    );
    public static void onClientTick(ClientTickEvent.Post event) {
        while (KICK_KEY.get().consumeClick()) {
            ClientPacketDistributor.sendToServer(new KickServerboundPayload());
        }
    }
}
*///?}