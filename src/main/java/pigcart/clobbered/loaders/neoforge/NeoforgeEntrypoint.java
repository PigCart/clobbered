//? if neoforge {
/*package pigcart.clobbered.loaders.neoforge;

import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.registries.RegisterEvent;
import pigcart.clobbered.config.gui.ConfigScreen;
import pigcart.clobbered.loaders.fabric.FabricClientEntrypoint;
import pigcart.clobbered.loaders.fabric.FabricEntrypoint;

import static pigcart.clobbered.Clobbered.MOD_ID;

@Mod(MOD_ID)
public class NeoforgeEntrypoint {

    public NeoforgeEntrypoint(IEventBus eventBus) {
        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (modContainer, parent) -> ConfigScreen.get(parent)
        );
        eventBus.addListener(RegisterEvent.class, NeoforgeEntrypoint::register);
    }

    public static void register(RegisterEvent event) {
        // yeah blocks whatever ffapi has it covered
        event.register(Registries.BLOCK, h -> {
            FabricEntrypoint.init();
            if (FMLEnvironment.dist.isClient()) {
                FabricClientEntrypoint.init();
            }
        });
    }
}
*///?}