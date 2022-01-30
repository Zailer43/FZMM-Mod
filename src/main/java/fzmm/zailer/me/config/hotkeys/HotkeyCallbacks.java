package fzmm.zailer.me.config.hotkeys;

public class HotkeyCallbacks {

    public static void init() {
        OpenGuiCallback openGuiCallback = new OpenGuiCallback();
        GenericCallback genericCallback = new GenericCallback();

        Hotkeys.FZMM_MAIN_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.CONFIG_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.CONVERTERS_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.ENCODEBOOK_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.GRADIENT_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.IMAGETEXT_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.IMAGETEXT_BOOK_PAGE_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.IMAGETEXT_BOOK_TOOLTIP_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.IMAGETEXT_HOLOGRAM_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.IMAGETEXT_LORE_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.IMAGETEXT_TELLRAW_GUI.getKeybind().setCallback(openGuiCallback);
        Hotkeys.PLAYER_STATUE_GUI.getKeybind().setCallback(openGuiCallback);

        Hotkeys.INCREMENT_GUI_SCALE.getKeybind().setCallback(genericCallback);
        Hotkeys.DECREMENT_GUI_SCALE.getKeybind().setCallback(genericCallback);
        Hotkeys.COPY_ITEM_NAME.getKeybind().setCallback(genericCallback);
        Hotkeys.COPY_ITEM_NAME_JSON.getKeybind().setCallback(genericCallback);
        Hotkeys.BACK_IN_FZMM_GUI.getKeybind().setCallback(genericCallback);
    }
}
