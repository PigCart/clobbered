package pigcart.clobbered.config.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import pigcart.clobbered.config.ConfigManager;

import java.util.List;
import java.util.Map;

import static pigcart.clobbered.config.gui.Widgets.*;

public class MapEntryWidget extends WidgetList.Row {

    Map<Object, Object> map;
    Object prevKey;

    /// creates a map entry with empty inputs
    public MapEntryWidget(ConfigScreen screen, Map<Object, Object> map) {
        this(screen, map, null, null);
    }

    public MapEntryWidget(ConfigScreen screen, Map<Object, Object> map, Object initialKey, Object initialValue) {
        super();
        this.map = map;
        this.prevKey = initialKey;
        // hardcoded for broken items map
        String keyString = getString(initialKey);
        String valueString = getString(initialValue);
        AbstractWidget keyInput = Widgets.getString(BUTTON_WIDTH, 0,
                "",
                keyString,
                this::onKeyChange,
                (val)-> Component.literal(val.toString())
        );
        AbstractWidget valueInput = Widgets.getString(BUTTON_WIDTH - BUTTON_HEIGHT, BUTTON_WIDTH + 10,
                "",
                valueString,
                this::onValueChange,
                (val)-> Component.literal(val.toString())
        );
        AbstractWidget removeButton = getButton(Component.literal("❌").withStyle(ChatFormatting.RED), (bttn) -> {
            map.remove(getKey());
            screen.refresh();
        });
        removeButton.setWidth(BUTTON_HEIGHT);
        ((AbstractWidgetAccess)removeButton).pigcart$setOffset(BIG_BUTTON_WIDTH - BUTTON_HEIGHT);
        AbstractWidget arrow = getLabel(Component.literal("→"));
        arrow.setX(BUTTON_WIDTH);
        this.widgets = List.of(keyInput, valueInput, removeButton, arrow);
    }

    private Object getKey() {
        InputWidget keyInput = (InputWidget) this.widgets.get(0);
        // hardcoded for broken item map
        return getObject(keyInput.getValue());
    }

    private Object getValue() {
        InputWidget valueInput = (InputWidget) this.widgets.get(1);
        // hardcoded for broken item map
        return getObject(valueInput.getValue());
    }

    public void onValueChange(String newValueStr) {
        map.put(getKey(), getObject(newValueStr));
    }

    public void onKeyChange(String newKeyStr) {
        final Object newKey = getObject(newKeyStr);
        if (map.containsKey(newKey)) {
            //throw new RuntimeException("Map already contains " + newKeyStr);
        } else {
            map.remove(prevKey);
            map.put(newKey, this.getValue());
        }
        prevKey = newKey;
    }

    /// hardcoded for broken item map
    public Object getObject(String string) {
        final Item item = ConfigManager.ItemMapTypeAdapter.getItem(string);
        if (item == Items.AIR) throw new RuntimeException("Invalid item: " + string);
        return item;
    }
    /// hardcoded for broken item map
    public String getString(Object object) {
        return object == null ? "" : ConfigManager.ItemMapTypeAdapter.getItemString((Item) object);
    }
}
