package pigcart.clobbered.config.gui;

import net.minecraft.network.chat.Component;

import java.text.NumberFormat;
import java.util.function.Function;

/// contains functions that are instantiated and run in response to config screen interactions
public class ConfigResponders {

    public static class Percent implements Function<Object, Component> {
        public Component apply(Object value) {
            return Component.literal(NumberFormat.getPercentInstance().format(value));
        }
    }

}
